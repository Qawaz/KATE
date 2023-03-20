package com.wakaztahir.kte.parser

import com.wakaztahir.kte.dsl.ModelIterable
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
            when (value) {
                is TemplateModel -> {
                    blockValue.model.putObject(elementConstName, value)
                }

                is ModelIterable<*> -> {
                    blockValue.model.putIterable(elementConstName, value)
                }

                is PrimitiveValue<*> -> {
                    blockValue.model.putValue(elementConstName, value)
                }

                else -> {
                    throw IllegalStateException("element of unknown type in for loop")
                }
            }
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
            while (index < total) {
                store(iterable.getOrElse(index) {
                    throw IllegalStateException("element at $index in for loop not found")
                })
                store(index)
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
            // println("INITIALIZER : $i,CONDITIONAL : $conditionValue,INCREMENTER : $incrementerValue,OPERATOR : ${arithmeticOperatorType.char},RESULT : ${conditionType.verifyCompare(i.compareTo(conditionValue))}")
            while (conditionType.verifyCompare(i.compareTo(conditionValue))) {
                storeIndex(i)
                block()
                i = arithmeticOperatorType.operate(i, incrementerValue)
                removeIndex()
            }
        }
    }

}

private fun SourceStream.parseForBlockValue(): LazyBlockSlice {
    val previous = pointer

    incrementUntil("@endfor")
    decrementPointer()

    val length = if (currentChar == ' ') {
        pointer - previous
    } else {
        pointer - previous + 1
    }

    incrementPointer()
    increment("@endfor")

    return LazyBlockSlice(
        startPointer = previous,
        length = length,
        parent = this.model,
    )
}

internal fun SourceStream.incrementBreakFor(): Boolean {
    return currentChar == '@' && increment("@breakfor")
}

private fun SourceStream.parseForLoopNumberProperty(): ReferencedValue? {
    parseConstantReference()?.let { return it }
    parseModelDirective()?.let { return it }
    parseNumberValue()?.let { return it }
    return null
}

private fun SourceStream.parseConditionalFor(): ForLoop.ConditionalFor? {
    val condition = parseCondition()
    if (condition != null) {
        increment(')')
        increment(' ')
        val blockValue = parseForBlockValue()
        return ForLoop.ConditionalFor(
            condition = condition,
            blockValue = blockValue
        )
    }
    return null
}

private fun SourceStream.parseIterableForLoopAfterVariable(variableName: String): ForLoop.IterableFor? {
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
        val blockValue = parseForBlockValue()
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

private fun SourceStream.parseNumberedForLoopAfterVariable(variableName: String): ForLoop.NumberedFor? {
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
                            val blockValue = parseForBlockValue()
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

internal fun SourceStream.parseForLoop(): ForLoop? {
    if (currentChar == '@' && increment("@for(")) {

        parseConditionalFor()?.let { return it }

        val variableName = parseConstantVariableName()
        if (variableName != null) {

            escapeSpaces()

            // Parsing the numbered loop
            parseNumberedForLoopAfterVariable(variableName)?.let { return it }

            // Parsing the iterable loop
            parseIterableForLoopAfterVariable(variableName)?.let { return it }

        }

    }
    return null
}