package com.wakaztahir.kate.parser

import com.wakaztahir.kate.dsl.ScopedModelLazyParent
import com.wakaztahir.kate.lexer.tokens.StaticTokens
import com.wakaztahir.kate.model.*
import com.wakaztahir.kate.model.ConditionType
import com.wakaztahir.kate.model.model.*
import com.wakaztahir.kate.parser.function.NestableInvocationBlock
import com.wakaztahir.kate.parser.stream.*
import com.wakaztahir.kate.parser.stream.increment
import com.wakaztahir.kate.parser.stream.parseTextWhile
import com.wakaztahir.kate.parser.variable.isVariableName
import com.wakaztahir.kate.parser.variable.parseVariableName
import com.wakaztahir.kate.parser.variable.parseVariableReference
import com.wakaztahir.kate.tokenizer.NodeTokenizer

sealed interface ForLoop : BlockContainer {

    override val parsedBlock: ForLoopParsedBlock

    fun iterate(model: MutableKATEObject, block: (iteration: Int) -> Unit)

    override fun generateTo(destination: DestinationStream) {
        parsedBlock.hasBroken = false
        parsedBlock.startInvocation()
        iterate(parsedBlock.provider.model) {
            parsedBlock.generateTo(destination = destination)
        }
        parsedBlock.endInvocation()
    }

    class ConditionalFor(
        val condition: ReferencedOrDirectValue,
        override val parsedBlock: ForLoopParsedBlock
    ) : ForLoop {
        override fun <T> selectNode(tokenizer: NodeTokenizer<T>) = tokenizer.conditionalFor
        override fun iterate(model: MutableKATEObject, block: (iteration: Int) -> Unit) {
            var i = 0
            while (!parsedBlock.hasBroken && (condition.asNullablePrimitive() as BooleanValue).value) {
                parsedBlock.haltGenFlag = false
                model.removeAll()
                block(i)
                i++
            }
        }
    }

    class IterableFor(
        val indexConstName: String?,
        val elementConstName: String,
        val listProperty: ReferencedOrDirectValue,
        override val parsedBlock: ForLoopParsedBlock
    ) : ForLoop {

        override fun <T> selectNode(tokenizer: NodeTokenizer<T>): T = tokenizer.iterableFor

        private fun store(model: MutableKATEObject, value: Int) {
            if (indexConstName != null) {
                model.insertValue(indexConstName, value)
            }
        }

        private fun store(model: MutableKATEObject, value: KATEValue) {
            model.insertValue(elementConstName, value)
        }

        private fun remove(model: MutableKATEObject) {
            if (indexConstName != null) {
                model.removeKey(indexConstName)
            }
            model.removeKey(elementConstName)
        }

        override fun iterate(model: MutableKATEObject, block: (iteration: Int) -> Unit) {
            var index = 0
            val iterable = listProperty.asNullableList()
                ?: throw IllegalStateException("list property is not iterable in for loop")
            val total = iterable.collection.size
            while (!parsedBlock.hasBroken && index < total) {
                parsedBlock.haltGenFlag = false
                model.removeAll()
                store(model, index)
                store(model, iterable.collection.getOrElse(index) {
                    throw IllegalStateException("element at $index in for loop not found")
                })
                block(index)
                index++
            }
            remove(model)
        }
    }

    class NumberedFor(
        val variableName: String,
        val initializer: ReferencedOrDirectValue,
        val conditionType: ConditionType,
        val conditional: ReferencedOrDirectValue,
        val arithmeticOperatorType: ArithmeticOperatorType,
        val incrementer: ReferencedOrDirectValue,
        override val parsedBlock: ForLoopParsedBlock
    ) : ForLoop {

        override fun <T> selectNode(tokenizer: NodeTokenizer<T>): T = tokenizer.numberedFor

        private fun ReferencedOrDirectValue.intVal(): Int {
            (asNullablePrimitive()?.value as? Int)?.let { return it }
                ?: throw IllegalStateException("for loop variable must be an integer")
        }

        private fun MutableKATEObject.storeIndex(value: Int) {
            require(insertValue(variableName, value)) {
                "couldn't insert index with name $variableName in numbered for loop with value $value"
            }
        }

        private fun MutableKATEObject.removeIndex() {
            removeKey(variableName)
        }

        override fun iterate(model: MutableKATEObject, block: (iteration: Int) -> Unit) {
            var i = initializer.intVal()
            val conditionValue = conditional.intVal()
            val incrementerValue = incrementer.intVal()
            while (!parsedBlock.hasBroken && conditionType.verifyCompare(i.compareTo(conditionValue))) {
                parsedBlock.haltGenFlag = false
                model.removeAll()
                model.storeIndex(i)
                block(i)
                i = arithmeticOperatorType.operate(i, incrementerValue)
            }
            model.removeIndex()
        }
    }

}

class ForLoopBreak(val slice: ForLoopParsedBlock) : CodeGen {
    override fun <T> selectNode(tokenizer: NodeTokenizer<T>): T = tokenizer.forLoopBreak
    override fun generateTo(destination: DestinationStream) {
        slice.haltGenFlag = true
        slice.hasBroken = true
    }
}

class ForLoopContinue(val block: ForLoopParsedBlock) : CodeGen {
    override fun <T> selectNode(tokenizer: NodeTokenizer<T>): T = tokenizer.forLoopContinue
    override fun generateTo(destination: DestinationStream) {
        block.haltGenFlag = true
    }
}

class ForLoopParsedBlock(parentProvider: ModelProvider,provider: ModelProvider.Changeable, codeGens: List<CodeGenRange>) : NestableInvocationBlock(
    parentProvider = parentProvider,
    provider = provider,
    codeGens = codeGens
) {
    // if true , stop iterating after this iteration is complete
    var hasBroken = false
    override fun onFunctionReturn() {
        super.onFunctionReturn()
        this.hasBroken = true
    }
}

class ForLoopLazyBlockSlice(
    parentBlock: LazyBlock,
    startPointer: Int,
    length: Int,
    blockEndPointer: Int,
    override val provider: ModelProvider.Changeable,
    isDefaultNoRaw: Boolean,
    indentationLevel: Int
) : LazyBlockSlice(
    parentBlock = parentBlock,
    startPointer = startPointer,
    length = length,
    provider = provider,
    blockEndPointer = blockEndPointer,
    isDefaultNoRaw = isDefaultNoRaw,
    indentationLevel = indentationLevel
) {

    private var parseTimes = 0
    val forLoopBlock = ForLoopParsedBlock(parentProvider = parentBlock.provider,provider = this.provider, mutableListOf())

    private fun ParserSourceStream.parseBreakForAtDirective(): ForLoopBreak? {
        return if (incrementDirective(StaticTokens.Break)) {
            // TODO remove this in some version
            if (increment("for")) {
                throw IllegalStateException("use new @break instead of @breakfor")
            }
            ForLoopBreak(forLoopBlock)
        } else {
            null
        }
    }

    private fun ParserSourceStream.parseContinueForAtDirective(): ForLoopContinue? {
        return if (incrementDirective(StaticTokens.Continue)) {
            ForLoopContinue(forLoopBlock)
        } else {
            null
        }
    }

    override fun parse(): ForLoopParsedBlock {
        parseTimes++
        if (parseTimes > 1) throw IllegalStateException("one instance can parse one block")
        (forLoopBlock.codeGens as MutableList).addAll(super.parse().codeGens)
        return forLoopBlock
    }

    override fun parseNestedAtDirective(block: LazyBlock): CodeGen? {
        source.parseBreakForAtDirective()?.let { return it }
        source.parseContinueForAtDirective()?.let { return it }
        return super.parseNestedAtDirective(block)
    }

}

private fun LazyBlock.parseForBlockValue(): ForLoopLazyBlockSlice {
    val slice = parseBlockSlice(
        startsWith = StaticTokens.For,
        endsWith = StaticTokens.EndFor,
        isDefaultNoRaw = isDefaultNoRaw,
        provider = ModelProvider.Changeable(ScopedModelLazyParent { model })
    )
    return ForLoopLazyBlockSlice(
        parentBlock = this,
        startPointer = slice.startPointer,
        length = slice.length,
        blockEndPointer = slice.blockEndPointer,
        provider = slice.provider as ModelProvider.Changeable,
        isDefaultNoRaw = slice.isDefaultNoRaw,
        indentationLevel = indentationLevel + 1
    )
}

private fun LazyBlock.parseConditionalFor(): ForLoop.ConditionalFor? {
    val condition = parseCondition(parseDirectRefs = false)
    if (condition != null) {
        source.increment(StaticTokens.RightParenthesis)
        source.increment(StaticTokens.SingleSpace)
        val blockValue = this@parseConditionalFor.parseForBlockValue()
        return ForLoop.ConditionalFor(
            condition = condition,
            parsedBlock = blockValue.parse()
        )
    }
    return null
}

private fun LazyBlock.parseListReferencedValue(parseDirectRefs: Boolean): ReferencedOrDirectValue? {
    parseListDefinition()?.let { return it }
    parseMutableListDefinition()?.let { return it }
    parseVariableReference(parseDirectRefs = parseDirectRefs)?.let { return it }
    return null
}

private fun LazyBlock.parseIterableForLoopAfterVariable(variableName: String): ForLoop.IterableFor? {
    var secondVariableName: String? = null
    if (source.increment(StaticTokens.Comma)) {
        secondVariableName = source.parseTextWhile { currentChar.isVariableName() }
    }
    source.escapeSpaces()
    if (source.increment(StaticTokens.Colon)) {
        source.escapeSpaces()
        val referencedValue = parseListReferencedValue(parseDirectRefs = true)
        source.escapeSpaces()
        if (!source.increment(StaticTokens.RightParenthesis)) {
            throw IllegalStateException("expected ${StaticTokens.RightParenthesis} , got ${source.currentChar}")
        }
        source.increment(StaticTokens.SingleSpace)
        val blockValue = this@parseIterableForLoopAfterVariable.parseForBlockValue()
        if (referencedValue != null) {
            return ForLoop.IterableFor(
                indexConstName = secondVariableName,
                elementConstName = variableName,
                listProperty = referencedValue,
                parsedBlock = blockValue.parse()
            )
        }
    }
    return null
}

private class NumberedForLoopIncrementer(
    val operatorType: ArithmeticOperatorType,
    val incrementerValue: ReferencedOrDirectValue
)

private fun LazyBlock.parseNumberOrReference(parseDirectRefs: Boolean): ReferencedOrDirectValue? {
    source.parseNumberValue()?.let { return it }
    parseVariableReference(parseDirectRefs = parseDirectRefs)?.let { return it }
    return null
}

private fun LazyBlock.parseNumberedForLoopIncrementer(variableName: String): NumberedForLoopIncrementer {
    val incrementalConst = source.parseTextWhile { currentChar.isVariableName() }
    if (incrementalConst == variableName) {
        val operator = source.parseArithmeticOperator()
        if (operator != null) {
            val singleIncrement =
                if (operator == ArithmeticOperatorType.Plus || operator == ArithmeticOperatorType.Minus) operator else null
            val singleIncrementerChar =
                if (singleIncrement == ArithmeticOperatorType.Plus) StaticTokens.Plus else if (singleIncrement == ArithmeticOperatorType.Minus) StaticTokens.Minus else null
            val incrementer = if (singleIncrement != null && source.increment(singleIncrementerChar!!)) {
                IntValue(1)
            } else {
                parseNumberOrReference(parseDirectRefs = true)
            }
                ?: throw IllegalStateException("expected number property or value or '+' or '-' , got ${source.currentChar} in for loop incrementer")
            return NumberedForLoopIncrementer(
                operatorType = operator,
                incrementerValue = incrementer
            )
        } else {
            throw IllegalStateException("expected '+','-','/','*','%' , got ${source.currentChar} in for loop condition")
        }
    } else {
        throw IllegalStateException("incremental variable is different : $incrementalConst != $variableName")
    }
}

private fun LazyBlock.parseNumberedForLoopAfterVariable(variableName: String): ForLoop.NumberedFor? {
    if (source.increment(StaticTokens.SingleEqual)) {
        source.escapeSpaces()
        val initializer = parseAnyExpressionOrValue()
            ?: throw IllegalStateException("unexpected ${source.currentChar} , expected a number or property")
        if (source.increment(StaticTokens.SemiColon)) {
            val conditionalConst = source.parseTextWhile { currentChar.isVariableName() }
            if (conditionalConst == variableName) {
                source.escapeSpaces()
                val conditionType = source.parseConditionType()
                    ?: throw IllegalStateException("expected conditional operator , got ${source.currentChar}")
                source.escapeSpaces()
                val conditional = parseAnyExpressionOrValue(
                    parseDirectRefs = true
                ) ?: throw IllegalStateException("expected number property of value got ${source.currentChar}")
                if (source.increment(StaticTokens.SemiColon)) {
                    val incrementer = parseNumberedForLoopIncrementer(variableName)
                    if (!source.increment(StaticTokens.RightParenthesis)) {
                        throw IllegalStateException("expected '${StaticTokens.RightParenthesis}' , got ${source.currentChar}")
                    }
                    source.increment(StaticTokens.SingleSpace)
                    val blockValue = this@parseNumberedForLoopAfterVariable.parseForBlockValue()
                    return ForLoop.NumberedFor(
                        variableName = variableName,
                        initializer = initializer,
                        conditionType = conditionType,
                        conditional = conditional,
                        arithmeticOperatorType = incrementer.operatorType,
                        incrementer = incrementer.incrementerValue,
                        parsedBlock = blockValue.parse()
                    )
                } else {
                    throw IllegalStateException("unexpected ${source.currentChar} , expected ';'")
                }
            } else {
                throw IllegalStateException("conditional variable is different : $conditionalConst != $variableName")
            }
        } else {
            throw IllegalStateException("unexpected ${source.currentChar} , expected ';'")
        }
    }
    return null
}

internal fun LazyBlock.parseForLoop(): ForLoop? {
    if (source.incrementDirective(StaticTokens.For)) {

        if(!source.increment(StaticTokens.LeftParenthesis)) {
            throw IllegalStateException("expected left parenthesis '(' after for loop")
        }

        parseConditionalFor()?.let { return it }

        val variableName = source.parseVariableName()
        if (variableName != null) {

            source.escapeSpaces()

            // Parsing the numbered loop
            parseNumberedForLoopAfterVariable(variableName)?.let { return it }

            // Parsing the iterable loop
            parseIterableForLoopAfterVariable(variableName)?.let { return it }

        }

    }
    return null
}