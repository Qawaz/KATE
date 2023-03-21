package com.wakaztahir.kte.parser

import com.wakaztahir.kte.dsl.ScopedModelObject
import com.wakaztahir.kte.model.model.ModelList
import com.wakaztahir.kte.model.model.MutableTemplateModel
import com.wakaztahir.kte.model.*
import com.wakaztahir.kte.model.ConditionType
import com.wakaztahir.kte.model.ReferencedValue
import com.wakaztahir.kte.model.model.TemplateModel
import com.wakaztahir.kte.parser.stream.*
import com.wakaztahir.kte.parser.stream.increment
import com.wakaztahir.kte.parser.stream.parseTextWhile

interface BreakableForBlockParser : BlockParser {

    var hasBroken: Boolean

    override fun hasNext(stream: SourceStream): Boolean {
        return !stream.hasEnded && !hasBroken
    }

    fun parseEndForAtDirective(source: SourceStream): Boolean {
        return if (source.currentChar == '@' && source.increment("@endfor")) {
            hasBroken = true
            true
        } else {
            false
        }
    }

    fun parseBreakForAtDirective(source: SourceStream): Boolean {
        return if (source.currentChar == '@' && source.increment("@breakfor")) {
            hasBroken = true
            true
        } else {
            false
        }
    }

    override fun parseAtDirective(source: SourceStream): AtDirective? = with(source) {
        if (parseBreakForAtDirective(source)) return null
        return super.parseAtDirective(source) ?: run {
            parseEndForAtDirective(source)
            null
        }
    }

}

internal sealed class ForLoop(val parser: BreakableForBlockParser) : AtDirective {

    val model get() = parser.model

    abstract fun iterate(model: MutableTemplateModel, block: () -> Unit)

    override fun generateTo(model: MutableTemplateModel, source: SourceStream, destination: DestinationStream) {
        iterate(model) { parser.generateTo(source, destination) }
    }

    class ConditionalFor(
        parser: BreakableForBlockParser,
        val condition: Condition
    ) : ForLoop(parser = parser) {
        override fun iterate(model: MutableTemplateModel, block: () -> Unit) {
            while (condition.evaluate(model)) {
                block()
            }
        }
    }

    class IterableFor(
        parser: BreakableForBlockParser,
        val indexConstName: String?,
        val elementConstName: String,
        val listProperty: ReferencedValue
    ) : ForLoop(parser = parser) {

        private fun store(value: Int) {
            if (indexConstName != null) {
                model.putValue(indexConstName, value)
            }
        }

        private fun store(value: KTEValue) {
            @Suppress("UNCHECKED_CAST")
            (value as? TemplateModel)?.let {
                model.putObject(elementConstName, it)
            } ?: (value as? ModelList<KTEValue>)?.let {
                model.putIterable(elementConstName, it)
            } ?: (value as? PrimitiveValue<*>)?.let {
                model.putValue(elementConstName, it)
            } ?: throw IllegalStateException("element of unknown type in for loop")
        }

        private fun remove() {
            if (indexConstName != null) {
                model.removeKey(indexConstName)
            }
            model.removeKey(elementConstName)
        }

        override fun iterate(model: MutableTemplateModel, block: () -> Unit) {
            var index = 0
            val iterable = listProperty.getIterable(model)
            val total = iterable.size
            if (total == 0) return
            while (index < total) {
                store(index)
                store(iterable.getOrElse(index) {
                    throw IllegalStateException("element at $index in iterable for loop not found")
                })
                block()
                index++
            }
            remove()
        }
    }

    class NumberedFor(
        parser: BreakableForBlockParser,
        val variableName: String,
        val initializer: ReferencedValue,
        val conditionType: ConditionType,
        val conditional: ReferencedValue,
        val arithmeticOperatorType: ArithmeticOperatorType,
        val incrementer: ReferencedValue,
    ) : ForLoop(parser = parser) {

        private fun ReferencedValue.intVal(context: MutableTemplateModel): Int {
            val value = getNullablePrimitive(context)
            if (value == null) {
                throw IllegalStateException("primitive value required inside for loop doesn't exist")
            } else {
                return (value as? IntValue)?.value?.let { return it }
                    ?: throw IllegalStateException("for loop variable must be an integer")
            }
        }

        private fun storeIndex(value: Int) {
            model.putValue(variableName, value)
        }

        private fun removeIndex() {
            model.removeKey(variableName)
        }

        override fun iterate(model: MutableTemplateModel, block: () -> Unit) {
            var i = initializer.intVal(model)
            val conditionValue = conditional.intVal(model)
            val incrementerValue = incrementer.intVal(model)
            while (conditionType.verifyCompare(i.compareTo(conditionValue))) {
                storeIndex(i)
                try {
                    block()
                } catch (e: Exception) {
                    println("EXCEPTION STATS")
                    println(
                        "I_AT : $i,CONDITIONAL : $conditionValue,INCREMENTER : $incrementerValue,OPERATOR : ${arithmeticOperatorType.char},RESULT : ${
                            conditionType.verifyCompare(
                                i.compareTo(conditionValue)
                            )
                        }"
                    )
                    throw e
                }
                i = arithmeticOperatorType.operate(i, incrementerValue)
            }
            removeIndex()
        }
    }

}

private fun SourceStream.parseForLoopNumberProperty(): ReferencedValue? {
    parseConstantReference()?.let { return it }
    parseModelDirective()?.let { return it }
    parseNumberValue()?.let { return it }
    return null
}

private fun SourceStream.parseConditionalForCondition(): Condition? {
    val condition = parseCondition()
    if (condition != null) {
        increment(')')
        increment(' ')
        return condition
    }
    return null
}

private class ForParser(parent: MutableTemplateModel) : BreakableForBlockParser {
    override val model = ScopedModelObject(parent = parent)
    override var hasBroken = false
}

private fun BlockParser.parseConditionalFor(source: SourceStream): ForLoop.ConditionalFor? {
    val condition = source.parseConditionalForCondition()
    if (condition != null) {
        return ForLoop.ConditionalFor(
            condition = condition,
            parser = ForParser(model)
        )
    }
    return null
}

private class IterableForStatement(
    val indexConstName: String?,
    val listProperty: ReferencedValue
)

private fun SourceStream.parseIterableForLoopStatementAfterVariableName(): IterableForStatement? {
    var secondVariableName: String? = null
    if (increment(',')) {
        secondVariableName = parseTextWhile { currentChar.isConstantVariableName() }
    }
    escapeSpaces()
    if (increment(':')) {
        escapeSpaces()
        val referencedValue = parseReferencedValue()
        escapeSpaces()
        if (!increment(')')) {
            throw IllegalStateException("expected ) , got $currentChar")
        }
        increment(' ')
        if (referencedValue != null) {
            return IterableForStatement(
                indexConstName = secondVariableName,
                listProperty = referencedValue
            )
        }
    }
    return null
}

private fun BlockParser.parseIterableForLoopAfterVariable(
    source: SourceStream,
    variableName: String
): ForLoop.IterableFor? {
    val statement = source.parseIterableForLoopStatementAfterVariableName()
    if (statement != null) {
        return ForLoop.IterableFor(
            parser = ForParser(model),
            indexConstName = statement.indexConstName,
            elementConstName = variableName,
            listProperty = statement.listProperty
        )
    }
    return null
}

private class NumberedForStatement(
    val initializer: ReferencedValue,
    val conditionType: ConditionType,
    val conditional: ReferencedValue,
    val arithmeticOperatorType: ArithmeticOperatorType,
    val incrementer: ReferencedValue,
)

private fun SourceStream.parseNumberedForLoopStatementAfterVariable(variableName: String): NumberedForStatement? {
    if (increment('=')) {
        escapeSpaces()
        val initializer = parseForLoopNumberProperty()
            ?: throw IllegalStateException("unexpected $currentChar , expected a number or property")
        if (increment(';')) {
            val conditionalConst = parseTextWhile { currentChar.isConstantVariableName() }
            if (conditionalConst == variableName) {
                val conditionType = parseConditionType()
                    ?: throw IllegalStateException("expected conditional operator , got $currentChar")
                val conditional = parseForLoopNumberProperty()
                    ?: throw IllegalStateException("expected number property of value got $currentChar")
                if (increment(';')) {
                    val incrementalConst = parseTextWhile { currentChar.isConstantVariableName() }
                    if (incrementalConst == variableName) {
                        val operator = parseArithmeticOperator()
                        if (operator != null) {
                            val incrementer = parseForLoopNumberProperty()
                                ?: throw IllegalStateException("expected number property of value got $currentChar")
                            if (!increment(')')) {
                                throw IllegalStateException("expected ) , got $currentChar")
                            }
                            increment(' ')
                            return NumberedForStatement(
                                initializer = initializer,
                                conditionType = conditionType,
                                conditional = conditional,
                                arithmeticOperatorType = operator,
                                incrementer = incrementer
                            )
                        } else {
                            throw IllegalStateException("expected '+','-','/','*','%' , got $operator")
                        }
                    } else {
                        throw IllegalStateException("incremental variable is different : $incrementalConst != $variableName")
                    }
                } else {
                    throw IllegalStateException("unexpected $currentChar , expected ';'")
                }
            } else {
                throw IllegalStateException("conditional variable is different : $conditionalConst != $variableName")
            }
        } else {
            throw IllegalStateException("unexpected $currentChar , expected ';'")
        }
    }
    return null
}

private fun BlockParser.parseNumberedForLoopAfterVariable(
    variableName: String,
    source: SourceStream
): ForLoop.NumberedFor? {
    val statement = source.parseNumberedForLoopStatementAfterVariable(variableName)
    if (statement != null) {
        return ForLoop.NumberedFor(
            parser = ForParser(model),
            variableName = variableName,
            initializer = statement.initializer,
            conditionType = statement.conditionType,
            conditional = statement.conditional,
            arithmeticOperatorType = statement.arithmeticOperatorType,
            incrementer = statement.incrementer
        )
    }
    return null
}

internal fun BlockParser.parseForLoop(source: SourceStream): ForLoop? {
    if (source.currentChar == '@' && source.increment("@for(")) {

        parseConditionalFor(source)?.let { return it }

        val variableName = source.parseConstantVariableName()
        if (variableName != null) {

            source.escapeSpaces()

            // Parsing the numbered loop
            parseNumberedForLoopAfterVariable(variableName, source = source)?.let { return it }

            // Parsing the iterable loop
            parseIterableForLoopAfterVariable(source = source, variableName)?.let { return it }

        }

    }
    return null
}