package com.wakaztahir.kate.parser

import com.wakaztahir.kate.lexer.model.StaticToken
import com.wakaztahir.kate.lexer.tokens.StaticTokens
import com.wakaztahir.kate.model.BooleanValue
import com.wakaztahir.kate.model.ConditionType
import com.wakaztahir.kate.model.LazyBlock
import com.wakaztahir.kate.model.model.KATEValue
import com.wakaztahir.kate.model.model.ReferencedOrDirectValue
import com.wakaztahir.kate.lexer.stream.increment
import com.wakaztahir.kate.model.expression.OperatorAssociativity
import com.wakaztahir.kate.parser.stream.ParserSourceStream

typealias ArithmeticOperatorType = com.wakaztahir.kate.model.expression.ArithmeticOperatorType

internal fun ParserSourceStream.parseArithmeticOperator(): ArithmeticOperatorType? {
    val result = when (currentChar) {
        // Arithmetic Operators
        '+' -> {
            incrementPointer()
            ArithmeticOperatorType.Plus
        }

        '-' -> {
            incrementPointer()
            ArithmeticOperatorType.Minus
        }

        '/' -> {
            incrementPointer()
            ArithmeticOperatorType.Divide
        }

        '*' -> {
            incrementPointer()
            ArithmeticOperatorType.Multiply
        }

        '%' -> {
            incrementPointer()
            ArithmeticOperatorType.Mod
        }
        // Logical Operators
        '&' -> {
            if (increment(StaticTokens.AndOperator)) ArithmeticOperatorType.And else null
        }

        '|' -> {
            if (increment(StaticTokens.OrOperator)) ArithmeticOperatorType.Or else null
        }
        // Conditional Operators
        '!' -> {
            return null
            if (increment(StaticTokens.NotEqual)) ArithmeticOperatorType.NotEqual else null
        }

        '=' -> {
            return null
            if (increment(StaticTokens.Equals)) {
                if (increment(StaticTokens.SingleEqual)) ArithmeticOperatorType.ReferentialEquality else ArithmeticOperatorType.Equal
            } else null
        }

        '>' -> {
            return null
            if (increment(StaticTokens.GreaterThanOrEqualTo)) {
                ArithmeticOperatorType.GreaterThanOrEqual
            } else ArithmeticOperatorType.GreaterThan
        }

        '<' -> {
            return null
            if (increment(StaticTokens.LessThanOrEqualTo)) ArithmeticOperatorType.LessThanOrEqual else ArithmeticOperatorType.LessThan
        }

        else -> null
    }
    return result
}

private class ValueAndOperatorStack {

    private val container = mutableListOf<Any>()

    fun isEmpty(): Boolean = container.isEmpty()

    fun putAllInto(other: ValueAndOperatorStack) {
        for (i in container.size - 1 downTo 0) other.container.add(container[i])
    }

    fun putValue(value: ReferencedOrDirectValue) {
        container.add(value)
    }

    fun putOperator(value: ArithmeticOperatorType) {
        container.add(value)
    }

    fun putCharacter(bracket: Char) {
        container.add(bracket)
    }

    fun peakOperator(): ArithmeticOperatorType? {
        return container.lastOrNull()?.let { it as? ArithmeticOperatorType }
    }

    fun peakValue(): ReferencedOrDirectValue? {
        return container.lastOrNull()?.let { it as? ReferencedOrDirectValue }
    }

    fun peakChar(): Char? {
        return container.lastOrNull()?.let { it as? Char }
    }

    fun popOperator(): ArithmeticOperatorType {
        return container.removeLast() as ArithmeticOperatorType
    }

    fun popValue(): ReferencedOrDirectValue {
        return container.removeLast() as ReferencedOrDirectValue
    }

    fun popChar(): Char {
        return container.removeLast() as Char
    }

    fun toExpression(): ExpressionValue? {
        val stack = ValueAndOperatorStack()
        while (container.isNotEmpty()) {
            when (val item = container.removeFirst()) {
                is ArithmeticOperatorType -> {
                    val second = stack.container.removeLast() as ReferencedOrDirectValue
                    val first = stack.container.removeLast() as ReferencedOrDirectValue
                    stack.putValue(
                        ExpressionValue(
                            first = first,
                            operatorType = item,
                            second = second,
                        )
                    )
                }

                is Char -> {

                }

                is ReferencedOrDirectValue -> {
                    stack.putValue(item)
                }

                else -> {
                    throw IllegalStateException("Unknown value type found in stack")
                }
            }
        }
        return stack.peakValue()?.let { it as? ExpressionValue }//?.also {
        //println("Expression : $it")
        //}
    }

}

private fun LazyBlock.parseValueAndOperator(
    valueParser: ExpressionValueParser
): Pair<ReferencedOrDirectValue, ArithmeticOperatorType?>? {
    val firstValue = with(valueParser) { parseExpressionValue() }
    if (firstValue != null) {
        val pointerAfterFirstValue = source.pointer
        source.increment(StaticTokens.SingleSpace)
        return if (!source.hasEnded) {
            val arithmeticOperator = source.parseArithmeticOperator()
            if (arithmeticOperator != null) {
                source.increment(StaticTokens.SingleSpace)
                Pair(firstValue, arithmeticOperator)
            } else {
                source.setPointerAt(pointerAfterFirstValue)
                Pair(firstValue, null)
            }
        } else {
            source.setPointerAt(pointerAfterFirstValue)
            Pair(firstValue, null)
        }
    }
    return null
}

private fun LazyBlock.parseExpressionWith(
    valueParser: ExpressionValueParser,
    stack: ValueAndOperatorStack,
    final: ValueAndOperatorStack
) {
    while (!source.hasEnded) {
        val valueAndOp = parseValueAndOperator(
            valueParser = valueParser
        )
        if (valueAndOp != null) {
            final.putValue(valueAndOp.first)
            val operator = valueAndOp.second
            if (operator != null) {
                if (stack.isEmpty() || stack.peakChar() == '(') {
                    stack.putOperator(operator)
                } else if (stack.peakOperator() == null || operator.precedence > stack.peakOperator()!!.precedence) {
                    while (!stack.isEmpty() && stack.peakOperator() != null) {
                        if (operator.precedence > stack.peakOperator()!!.precedence) {
                            final.putOperator(stack.popOperator())
                        } else if (operator.precedence == stack.peakOperator()!!.precedence) {
                            when (operator.associativity) {
                                OperatorAssociativity.LeftToRight -> final.putOperator(stack.popOperator())
                                OperatorAssociativity.RightToLeft -> {
                                    // do nothing
                                }
                            }
                        }
                    }
                    stack.putOperator(operator)
                } else if (operator.precedence < stack.peakOperator()!!.precedence) {
                    stack.putOperator(operator)
                } else if (operator.precedence == stack.peakOperator()!!.precedence) {
                    when (operator.associativity) {
                        OperatorAssociativity.LeftToRight -> final.putOperator(stack.popOperator())
                        OperatorAssociativity.RightToLeft -> {
                            // do nothing
                        }
                    }
                    stack.putOperator(operator)
                } else {
                    throw IllegalStateException("no condition satisfied")
                }
            } else {
                break
            }
        } else {
            if (source.currentChar == '(') {
                stack.putCharacter(source.currentChar)
                source.incrementPointer()
            } else if (source.currentChar == ')') {
                var found = false
                while (!found) {
                    if (stack.peakOperator() != null) {
                        final.putOperator(stack.popOperator())
                    } else if (stack.peakChar() != null) {
                        if (stack.peakChar() == '(') found = true
                        final.putCharacter(stack.popChar())
                    }
                }
                source.incrementPointer()
            } else {
                break
            }
        }
    }
    stack.putAllInto(final)
}

internal fun LazyBlock.parseExpressionAfter(
    value: ReferencedOrDirectValue,
    operator: ArithmeticOperatorType,
    valueParser: ExpressionValueParser,
): ExpressionValue? {
    val stack = ValueAndOperatorStack()
    stack.putOperator(operator)
    val final = ValueAndOperatorStack()
    final.putValue(value)
    parseExpressionWith(
        valueParser = valueParser,
        stack = stack,
        final = final
    )
    return final.toExpression()
}

internal fun LazyBlock.parseExpression(valueParser: ExpressionValueParser): ReferencedOrDirectValue? {
    val valueAndOp = parseValueAndOperator(
        valueParser = valueParser
    )
    if (valueAndOp != null) {
        return if (valueAndOp.second != null) {
            return parseExpressionAfter(
                value = valueAndOp.first,
                operator = valueAndOp.second!!,
                valueParser = valueParser
            )
        } else {
            valueAndOp.first
        }
    }
    return null
}

internal fun LazyBlock.parseExpression(parseDirectRefs: Boolean): ReferencedOrDirectValue? = parseExpression(
    valueParser = DefaultExpressionValueParser(parseDirectRefs = parseDirectRefs)
)

internal fun LazyBlock.parseAnyExpressionOrValue(parseDirectRefs: Boolean = true): ReferencedOrDirectValue? {
    parseListDefinition()?.let { return it }
    parseMutableListDefinition()?.let { return it }
    parseExpression(parseDirectRefs = parseDirectRefs)?.let { return it }
    return null
}