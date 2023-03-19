package com.wakaztahir.kte.parser

import com.wakaztahir.kte.model.*
import com.wakaztahir.kte.model.ConditionType
import com.wakaztahir.kte.model.DynamicProperty
import com.wakaztahir.kte.model.ReferencedValue
import com.wakaztahir.kte.parser.stream.*
import com.wakaztahir.kte.parser.stream.increment
import com.wakaztahir.kte.parser.stream.parseTextWhile

internal sealed interface ForLoop : AtDirective {

    val blockValue: LazyBlockSlice

    class ConditionalFor(
        val condition: Condition,
        override val blockValue: LazyBlockSlice
    ) : ForLoop

    class IterableFor(
        val indexConstName: String?,
        val elementConstName: String,
        val listProperty: ReferencedValue,
        override val blockValue: LazyBlockSlice
    ) : ForLoop

    class NumberedFor(
        val variableName: String,
        val initializer: DynamicProperty,
        val conditionType: ConditionType,
        val conditional: DynamicProperty,
        val arithmeticOperatorType: ArithmeticOperatorType,
        val incrementer: DynamicProperty,
        override val blockValue: LazyBlockSlice
    ) : ForLoop

}

private fun SourceStream.parseForBlockValue(): LazyBlockSlice {
    val previous = pointer

    incrementUntil("@endfor")
    decrementPointer()

    val length = if(currentChar == ' '){
        pointer - previous
    }else {
        pointer - previous + 1
    }

    incrementPointer()
    increment("@endfor")

    return LazyBlockSlice(
        pointer = previous,
        length = length
    )
//    return parseTextUntilConsumed("@endfor").let {
//        if (it.lastOrNull() == ' ') it.substringBeforeLast(' ') else it
//    }
}

internal fun SourceStream.incrementBreakFor(): Boolean {
    return currentChar == '@' && increment("@breakfor")
}

private fun SourceStream.parseForLoopNumberProperty(): DynamicProperty? {
    parseConstantReference()?.let { return DynamicProperty(property = it, value = null) }
    parseModelDirective()?.let { return DynamicProperty(property = it, value = null) }
    parseNumberValue()?.let { return DynamicProperty(property = null, value = it) }
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
        if(!increment(')')){
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