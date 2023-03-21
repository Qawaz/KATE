package com.wakaztahir.kte.parser

import com.wakaztahir.kte.model.model.ModelList
import com.wakaztahir.kte.model.model.MutableTemplateModel
import com.wakaztahir.kte.model.*
import com.wakaztahir.kte.model.ConditionType
import com.wakaztahir.kte.model.ReferencedValue
import com.wakaztahir.kte.model.model.TemplateModel
import com.wakaztahir.kte.parser.stream.*
import com.wakaztahir.kte.parser.stream.increment
import com.wakaztahir.kte.parser.stream.parseTextWhile

internal sealed interface ForLoop : AtDirective {

    val blockValue: LazyBlockSlice

    fun iterate(context: MutableTemplateModel, block: () -> Unit)

    override fun generateTo(block: LazyBlock, source: SourceStream, destination: DestinationStream) {
        iterate(block.model) {
            blockValue.generateTo(source = source, destination = destination)
        }
    }

    class ConditionalFor(
        val condition: Condition,
        override val blockValue: LazyBlockSlice
    ) : ForLoop {
        override fun iterate(context: MutableTemplateModel, block: () -> Unit) {
            while (condition.evaluate(context)) {
                block()
            }
        }
    }

    class IterableFor(
        val indexConstName: String?,
        val elementConstName: String,
        val listProperty: ReferencedValue,
        override val blockValue: LazyBlockSlice
    ) : ForLoop {

        private fun store(value: Int) {
            if (indexConstName != null) {
                blockValue.model.putValue(indexConstName, value)
            }
        }

        private fun store(value: KTEValue) {
            @Suppress("UNCHECKED_CAST")
            (value as? TemplateModel)?.let {
                blockValue.model.putObject(elementConstName, it)
            } ?: (value as? ModelList<KTEValue>)?.let {
                blockValue.model.putIterable(elementConstName, it)
            } ?: (value as? PrimitiveValue<*>)?.let {
                blockValue.model.putValue(elementConstName, it)
            } ?: throw IllegalStateException("element of unknown type in for loop")
        }

        private fun remove() {
            if (indexConstName != null) {
                blockValue.model.removeKey(indexConstName)
            }
            blockValue.model.removeKey(elementConstName)
        }

        override fun iterate(context: MutableTemplateModel, block: () -> Unit) {
            var index = 0
            val iterable = listProperty.getIterable(context)
            val total = iterable.size
//            println("ITERABLE SIZE : $total")
            while (index < total) {
                store(index)
                store(iterable.getOrElse(index) {
                    throw IllegalStateException("element at $index in for loop not found")
                })
                block()
                index++
            }
            remove()
        }
    }

    class NumberedFor(
        val variableName: String,
        val initializer: ReferencedValue,
        val conditionType: ConditionType,
        val conditional: ReferencedValue,
        val arithmeticOperatorType: ArithmeticOperatorType,
        val incrementer: ReferencedValue,
        override val blockValue: LazyBlockSlice
    ) : ForLoop {

        private fun ReferencedValue.intVal(context: MutableTemplateModel): Int {
            (getValue(context) as? IntValue)?.value?.let { return it }
                ?: throw IllegalStateException("for loop variable must be an integer")
        }

        private fun storeIndex(value: Int) {
            blockValue.model.putValue(variableName, value)
        }

        private fun removeIndex() {
            blockValue.model.removeKey(variableName)
        }

        override fun iterate(context: MutableTemplateModel, block: () -> Unit) {
            var i = initializer.intVal(context)
            val conditionValue = conditional.intVal(context)
            val incrementerValue = incrementer.intVal(context)
            while (conditionType.verifyCompare(i.compareTo(conditionValue))) {
//                println(
//                    "INITIALIZER : $i,CONDITIONAL : $conditionValue,INCREMENTER : $incrementerValue,OPERATOR : ${arithmeticOperatorType.char},RESULT : ${
//                        conditionType.verifyCompare(
//                            i.compareTo(conditionValue)
//                        )
//                    }"
//                )
                storeIndex(i)
                block()
                i = arithmeticOperatorType.operate(i, incrementerValue)
            }
            removeIndex()
        }
    }

}

private class ForLoopLazyBlockSlice(
    startPointer: Int,
    length: Int,
    blockEndPointer: Int,
    parent: MutableTemplateModel,
) : LazyBlockSlice(
    startPointer = startPointer,
    length = length,
    parent = parent,
    blockEndPointer = blockEndPointer
) {

    var hasBroken: Boolean = false

    override fun canIterate(stream: SourceStream): Boolean {
        return super.canIterate(stream) && !hasBroken
    }

    fun SourceStream.parseBreakForAtDirective(): Boolean {
        return if (currentChar == '@' && increment("@breakfor")) {
            hasBroken = true
            true
        } else {
            false
        }
    }

    override fun parseAtDirective(source: SourceStream): AtDirective? = with(source) {
        if (parseBreakForAtDirective()) return null
        return super.parseAtDirective(source)
    }

}

private fun LazyBlock.parseForBlockValue(source: SourceStream): LazyBlockSlice  {
    val previous = source.pointer

    val ender: String = source.incrementUntilDirectiveWithSkip("@for") {
        if (source.increment("@endfor")) "@endfor" else null
    } ?: throw IllegalStateException("@for must end with @endfor")

    source.decrementPointer(ender.length)

    val length = source.pointer - previous

    source.decrementPointer()
    val spaceDecrement = if (source.currentChar == ' ') 1 else 0
    source.incrementPointer()

    source.increment(ender)

    return ForLoopLazyBlockSlice(
        startPointer = previous,
        length = length - spaceDecrement,
        parent = this@parseForBlockValue.model,
        blockEndPointer = source.pointer
    )
}

private fun SourceStream.parseForLoopNumberProperty(): ReferencedValue? {
    parseConstantReference()?.let { return it }
    parseModelDirective()?.let { return it }
    parseNumberValue()?.let { return it }
    return null
}

private fun LazyBlock.parseConditionalFor(source: SourceStream): ForLoop.ConditionalFor? {
    val condition = source.parseCondition()
    if (condition != null) {
        source.increment(')')
        source.increment(' ')
        val blockValue = this@parseConditionalFor.parseForBlockValue(source)
        return ForLoop.ConditionalFor(
            condition = condition,
            blockValue = blockValue
        )
    }
    return null
}

private fun LazyBlock.parseIterableForLoopAfterVariable(
    source: SourceStream,
    variableName: String,
): ForLoop.IterableFor?  {
    var secondVariableName: String? = null
    if (source.increment(',')) {
        secondVariableName = source.parseTextWhile { currentChar.isConstantVariableName() }
    }
    source.escapeSpaces()
    if (source.increment(':')) {
        source.escapeSpaces()
        val referencedValue = source.parseReferencedValue()
        source.escapeSpaces()
        if (!source.increment(')')) {
            throw IllegalStateException("expected ) , got ${source.currentChar}")
        }
        source.increment(' ')
        val blockValue = this@parseIterableForLoopAfterVariable.parseForBlockValue(source)
        if (referencedValue != null) {
            return ForLoop.IterableFor(
                indexConstName = secondVariableName,
                elementConstName = variableName,
                listProperty = referencedValue,
                blockValue = blockValue
            )
        }
    }
    return null
}

private fun LazyBlock.parseNumberedForLoopAfterVariable(
    source: SourceStream,
    variableName: String
): ForLoop.NumberedFor? {
    if (source.increment('=')) {
        source.escapeSpaces()
        val initializer = source.parseForLoopNumberProperty()
            ?: throw IllegalStateException("unexpected ${source.currentChar} , expected a number or property")
        if (source.increment(';')) {
            val conditionalConst = source.parseTextWhile { currentChar.isConstantVariableName() }
            if (conditionalConst == variableName) {
                val conditionType = source.parseConditionType()
                    ?: throw IllegalStateException("expected conditional operator , got ${source.currentChar}")
                val conditional = source.parseForLoopNumberProperty()
                    ?: throw IllegalStateException("expected number property of value got ${source.currentChar}")
                if (source.increment(';')) {
                    val incrementalConst = source.parseTextWhile { currentChar.isConstantVariableName() }
                    if (incrementalConst == variableName) {
                        val operator = source.parseArithmeticOperator()
                        if (operator != null) {
                            val incrementer = source.parseForLoopNumberProperty()
                                ?: throw IllegalStateException("expected number property of value got ${source.currentChar}")
                            if (!source.increment(')')) {
                                throw IllegalStateException("expected ) , got ${source.currentChar}")
                            }
                            source.increment(' ')
                            val blockValue = this@parseNumberedForLoopAfterVariable.parseForBlockValue(source)
                            return ForLoop.NumberedFor(
                                variableName = variableName,
                                initializer = initializer,
                                conditionType = conditionType,
                                conditional = conditional,
                                arithmeticOperatorType = operator,
                                incrementer = incrementer,
                                blockValue = blockValue
                            )
                        } else {
                            throw IllegalStateException("expected '+','-','/','*','%' , got $operator")
                        }
                    } else {
                        throw IllegalStateException("incremental variable is different : $incrementalConst != $variableName")
                    }
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

internal fun LazyBlock.parseForLoop(source: SourceStream): ForLoop? {
    if (source.currentChar == '@' && source.increment("@for(")) {

        parseConditionalFor(source)?.let { return it }

        val variableName = source.parseConstantVariableName()
        if (variableName != null) {

            source.escapeSpaces()

            // Parsing the numbered loop
            parseNumberedForLoopAfterVariable(source, variableName)?.let { return it }

            // Parsing the iterable loop
            parseIterableForLoopAfterVariable(source, variableName)?.let { return it }

        }

    }
    return null
}