package com.wakaztahir.kate.parser

import com.wakaztahir.kate.lexer.tokens.CharStaticToken
import com.wakaztahir.kate.lexer.tokens.StaticTokens
import com.wakaztahir.kate.model.BooleanValue
import com.wakaztahir.kate.model.ConditionType
import com.wakaztahir.kate.model.LazyBlock
import com.wakaztahir.kate.model.model.KATEValue
import com.wakaztahir.kate.model.model.ReferencedOrDirectValue
import com.wakaztahir.kate.parser.stream.ParserSourceStream
import com.wakaztahir.kate.parser.stream.increment

enum class OperatorAssociativity {
    LeftToRight,
    RightToLeft
}

enum class ArithmeticOperatorType(val char: String, val associativity: OperatorAssociativity, val precedence: Int) {

    Plus("+", associativity = OperatorAssociativity.LeftToRight, precedence = 6) {
        override fun operate(value1: Int, value2: Int): Int = value1 + value2
        override fun operate(value1: Double, value2: Double): Double = value1 + value2
        override fun operate(value1: Int, value2: Double): Double = value1 + value2
        override fun operate(value1: Int, value2: Long): Long = value1 + value2
        override fun operate(value1: Double, value2: Int): Double = value1 + value2
        override fun operate(value1: Double, value2: Long): Double = value1 + value2
        override fun operate(value1: Long, value2: Long): Long = value1 + value2
        override fun operate(value1: Long, value2: Int): Long = value1 + value2
        override fun operate(value1: Long, value2: Double): Double = value1 + value2
        override fun operate(value1: String, value2: String): String = value1 + value2
        override fun operate(value1: String, value2: Int): String = value1 + value2
        override fun operate(value1: String, value2: Double): String = value1 + value2
        override fun operate(value1: String, value2: Char): String = value1 + value2
        override fun operate(value1: Char, value2: Int): Char = value1 + value2
        override fun operate(value1: Char, value2: String): String = value1 + value2

    },
    Minus("-", associativity = OperatorAssociativity.LeftToRight, precedence = 6) {
        override fun operate(value1: Int, value2: Int): Int = value1 - value2
        override fun operate(value1: Double, value2: Double): Double = value1 - value2
        override fun operate(value1: Int, value2: Double): Double = value1 - value2
        override fun operate(value1: Int, value2: Long): Long = value1 - value2
        override fun operate(value1: Double, value2: Int): Double = value1 - value2
        override fun operate(value1: Double, value2: Long): Double = value1 - value2
        override fun operate(value1: Long, value2: Long): Long = value1 - value2
        override fun operate(value1: Long, value2: Int): Long = value1 - value2
        override fun operate(value1: Long, value2: Double): Double = value1 - value2
        override fun operate(value1: Char, value2: Char): Int = value1 - value2
    },
    Divide("/", associativity = OperatorAssociativity.LeftToRight, precedence = 4) {
        override fun operate(value1: Int, value2: Int): Int = value1 / value2
        override fun operate(value1: Double, value2: Double): Double = value1 / value2
        override fun operate(value1: Int, value2: Double): Double = value1 / value2
        override fun operate(value1: Int, value2: Long): Long = value1 / value2
        override fun operate(value1: Double, value2: Int): Double = value1 / value2
        override fun operate(value1: Double, value2: Long): Double = value1 / value2
        override fun operate(value1: Long, value2: Long): Long = value1 / value2
        override fun operate(value1: Long, value2: Int): Long = value1 / value2
        override fun operate(value1: Long, value2: Double): Double = value1 / value2
    },
    Multiply("*", associativity = OperatorAssociativity.LeftToRight, precedence = 4) {
        override fun operate(value1: Int, value2: Int): Int = value1 * value2
        override fun operate(value1: Double, value2: Double): Double = value1 * value2
        override fun operate(value1: Int, value2: Double): Double = value1 * value2
        override fun operate(value1: Int, value2: Long): Long = value1 * value2
        override fun operate(value1: Double, value2: Int): Double = value1 * value2
        override fun operate(value1: Double, value2: Long): Double = value1 * value2
        override fun operate(value1: Long, value2: Long): Long = value1 * value2
        override fun operate(value1: Long, value2: Int): Long = value1 * value2
        override fun operate(value1: Long, value2: Double): Double = value1 * value2
    },
    Mod("%", associativity = OperatorAssociativity.LeftToRight, precedence = 4) {
        override fun operate(value1: Int, value2: Int): Int = value1 % value2
        override fun operate(value1: Double, value2: Double): Double = value1 % value2
        override fun operate(value1: Int, value2: Double): Double = value1 % value2
        override fun operate(value1: Int, value2: Long): Long = value1 % value2
        override fun operate(value1: Double, value2: Int): Double = value1 % value2
        override fun operate(value1: Double, value2: Long): Double = value1 % value2
        override fun operate(value1: Long, value2: Long): Long = value1 % value2
        override fun operate(value1: Long, value2: Int): Long = value1 % value2
        override fun operate(value1: Long, value2: Double): Double = value1 % value2
    },
    And("&&", associativity = OperatorAssociativity.LeftToRight, precedence = 2) {
        override fun operate(value1: Boolean, value2: Boolean): Boolean = value1 && value2
    },
    Or("||", associativity = OperatorAssociativity.LeftToRight, precedence = 1) {
        override fun operate(value1: Boolean, value2: Boolean): Boolean = value1 || value2
    },
    Equal("==", associativity = OperatorAssociativity.LeftToRight, precedence = 3) {
        override fun operate(value1: KATEValue, value2: KATEValue): KATEValue =
            BooleanValue(ConditionType.Equals.verifyCompare(value1.compareTo(value2)))
    },
    NotEqual("!=", associativity = OperatorAssociativity.LeftToRight, precedence = 3) {
        override fun operate(value1: KATEValue, value2: KATEValue): KATEValue =
            BooleanValue(ConditionType.NotEquals.verifyCompare(value1.compareTo(value2)))
    },
    LessThanOrEqual("<=", associativity = OperatorAssociativity.LeftToRight, precedence = 5) {
        override fun operate(value1: KATEValue, value2: KATEValue): KATEValue =
            BooleanValue(ConditionType.LessThanEqualTo.verifyCompare(value1.compareTo(value2)))
    },
    LessThan("<", associativity = OperatorAssociativity.LeftToRight, precedence = 5) {
        override fun operate(value1: KATEValue, value2: KATEValue): KATEValue =
            BooleanValue(ConditionType.LessThan.verifyCompare(value1.compareTo(value2)))
    },
    GreaterThan(">", associativity = OperatorAssociativity.LeftToRight, precedence = 5) {
        override fun operate(value1: KATEValue, value2: KATEValue): KATEValue =
            BooleanValue(ConditionType.GreaterThan.verifyCompare(value1.compareTo(value2)))
    },
    GreaterThanOrEqual(">=", associativity = OperatorAssociativity.LeftToRight, precedence = 5) {
        override fun operate(value1: KATEValue, value2: KATEValue): KATEValue =
            BooleanValue(ConditionType.GreaterThanEqualTo.verifyCompare(value1.compareTo(value2)))
    },
    ReferentialEquality("===", associativity = OperatorAssociativity.LeftToRight, precedence = 3) {
        override fun operate(value1: KATEValue, value2: KATEValue): KATEValue =
            BooleanValue(ConditionType.ReferentiallyEquals.verifyCompare(value1.compareTo(value2)))
    };

    private fun notPossible(value1: Any, value2: Any): Nothing {
        throw IllegalStateException("operation : $char is not possible between $value1 and $value2")
    }

    open fun operate(value1: Boolean, value2: Boolean): Boolean = notPossible(value1, value2)
    open fun operate(value1: Int, value2: Int): Int = notPossible(value1, value2)
    open fun operate(value1: Int, value2: Double): Double = notPossible(value1, value2)
    open fun operate(value1: Int, value2: Long): Long = notPossible(value1, value2)
    open fun operate(value1: Double, value2: Double): Double = notPossible(value1, value2)
    open fun operate(value1: Double, value2: Int): Double = notPossible(value1, value2)
    open fun operate(value1: Double, value2: Long): Double = notPossible(value1, value2)
    open fun operate(value1: Long, value2: Long): Long = notPossible(value1, value2)
    open fun operate(value1: Long, value2: Int): Long = notPossible(value1, value2)
    open fun operate(value1: Long, value2: Double): Double = notPossible(value1, value2)
    open fun operate(value1: String, value2: String): String = notPossible(value1, value2)
    open fun operate(value1: String, value2: Int): String = notPossible(value1, value2)
    open fun operate(value1: String, value2: Double): String = notPossible(value1, value2)
    open fun operate(value1: String, value2: Char): String = notPossible(value1, value2)
    open fun operate(value1: Char, value2: Char): Int = notPossible(value1, value2)
    open fun operate(value1: Char, value2: Int): Char = notPossible(value1, value2)
    open fun operate(value1: Char, value2: String): String = notPossible(value1, value2)
    open fun operate(value1: KATEValue, value2: KATEValue): KATEValue = notPossible(value1, value2)

}

private fun ParserSourceStream.incrementTwoChars(char1: CharStaticToken, char2: CharStaticToken): Boolean {
    return if (increment(char1)) {
        if (increment(char2)) {
            true
        } else {
            decrementPointer()
            false
        }
    } else {
        false
    }
}

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

internal fun LazyBlock.parseExpression(parseDirectRefs: Boolean): ReferencedOrDirectValue? = parseExpression(
    valueParser = DefaultExpressionValueParser(parseDirectRefs = parseDirectRefs)
)

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

internal fun LazyBlock.parseAnyExpressionOrValue(parseDirectRefs: Boolean = true): ReferencedOrDirectValue? {
    parseListDefinition()?.let { return it }
    parseMutableListDefinition()?.let { return it }
    parseExpression(parseDirectRefs = parseDirectRefs)?.let { return it }
    return null
}