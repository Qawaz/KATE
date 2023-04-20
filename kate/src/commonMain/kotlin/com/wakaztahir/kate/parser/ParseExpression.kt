package com.wakaztahir.kate.parser

import com.wakaztahir.kate.model.LazyBlock
import com.wakaztahir.kate.model.model.ReferencedValue
import com.wakaztahir.kate.parser.stream.SourceStream
import com.wakaztahir.kate.parser.stream.increment

enum class OperatorAssociativity {
    LeftToRight,
    RightToLeft
}

enum class ArithmeticOperatorType(val char: Char, val associativity: OperatorAssociativity, val precedence: Int) {

    Plus('+', associativity = OperatorAssociativity.LeftToRight, precedence = 6) {
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
        override fun operate(value1: Char, value2: Char): Int = notPossible("Char", "Char")
        override fun operate(value1: Char, value2: Int): Char = value1 + value2
        override fun operate(value1: Char, value2: String): String = value1 + value2
    },
    Minus('-', associativity = OperatorAssociativity.LeftToRight, precedence = 6) {
        override fun operate(value1: Int, value2: Int): Int = value1 - value2
        override fun operate(value1: Double, value2: Double): Double = value1 - value2
        override fun operate(value1: Int, value2: Double): Double = value1 - value2
        override fun operate(value1: Int, value2: Long): Long = value1 - value2
        override fun operate(value1: Double, value2: Int): Double = value1 - value2
        override fun operate(value1: Double, value2: Long): Double = value1 - value2
        override fun operate(value1: Long, value2: Long): Long = value1 - value2
        override fun operate(value1: Long, value2: Int): Long = value1 - value2
        override fun operate(value1: Long, value2: Double): Double = value1 - value2
        override fun operate(value1: String, value2: String): String = notPossible("String", "String")
        override fun operate(value1: String, value2: Int): String = notPossible("String", "Int")
        override fun operate(value1: String, value2: Double): String = notPossible("String", "Double")
        override fun operate(value1: String, value2: Char): String = notPossible("String", "Char")
        override fun operate(value1: Char, value2: Char): Int = value1 - value2
        override fun operate(value1: Char, value2: Int): Char = notPossible("Char", "Int")
        override fun operate(value1: Char, value2: String): String = notPossible("Char", "String")
    },
    Divide('/', associativity = OperatorAssociativity.LeftToRight, precedence = 4) {
        override fun operate(value1: Int, value2: Int): Int = value1 / value2
        override fun operate(value1: Double, value2: Double): Double = value1 / value2
        override fun operate(value1: Int, value2: Double): Double = value1 / value2
        override fun operate(value1: Int, value2: Long): Long = value1 / value2
        override fun operate(value1: Double, value2: Int): Double = value1 / value2
        override fun operate(value1: Double, value2: Long): Double = value1 / value2
        override fun operate(value1: Long, value2: Long): Long = value1 / value2
        override fun operate(value1: Long, value2: Int): Long = value1 / value2
        override fun operate(value1: Long, value2: Double): Double = value1 / value2
        override fun operate(value1: String, value2: String): String = notPossible("String", "String")
        override fun operate(value1: String, value2: Int): String = notPossible("String", "Int")
        override fun operate(value1: String, value2: Double): String = notPossible("String", "Double")
        override fun operate(value1: String, value2: Char): String = notPossible("String", "Char")
        override fun operate(value1: Char, value2: Char): Int = notPossible("Char", "Char")
        override fun operate(value1: Char, value2: Int): Char = notPossible("Char", "Int")
        override fun operate(value1: Char, value2: String): String = notPossible("Char", "String")
    },
    Multiply('*', associativity = OperatorAssociativity.LeftToRight, precedence = 4) {
        override fun operate(value1: Int, value2: Int): Int = value1 * value2
        override fun operate(value1: Double, value2: Double): Double = value1 * value2
        override fun operate(value1: Int, value2: Double): Double = value1 * value2
        override fun operate(value1: Int, value2: Long): Long = value1 * value2
        override fun operate(value1: Double, value2: Int): Double = value1 * value2
        override fun operate(value1: Double, value2: Long): Double = value1 * value2
        override fun operate(value1: Long, value2: Long): Long = value1 * value2
        override fun operate(value1: Long, value2: Int): Long = value1 * value2
        override fun operate(value1: Long, value2: Double): Double = value1 * value2
        override fun operate(value1: String, value2: String): String = notPossible("String", "String")
        override fun operate(value1: String, value2: Int): String = notPossible("String", "Int")
        override fun operate(value1: String, value2: Double): String = notPossible("String", "Double")
        override fun operate(value1: String, value2: Char): String = notPossible("String", "Char")
        override fun operate(value1: Char, value2: Char): Int = notPossible("Char", "Char")
        override fun operate(value1: Char, value2: Int): Char = notPossible("Char", "Int")
        override fun operate(value1: Char, value2: String): String = notPossible("Char", "String")
    },
    Mod('%', associativity = OperatorAssociativity.LeftToRight, precedence = 4) {
        override fun operate(value1: Int, value2: Int): Int = value1 % value2
        override fun operate(value1: Double, value2: Double): Double = value1 % value2
        override fun operate(value1: Int, value2: Double): Double = value1 % value2
        override fun operate(value1: Int, value2: Long): Long = value1 % value2
        override fun operate(value1: Double, value2: Int): Double = value1 % value2
        override fun operate(value1: Double, value2: Long): Double = value1 % value2
        override fun operate(value1: Long, value2: Long): Long = value1 % value2
        override fun operate(value1: Long, value2: Int): Long = value1 % value2
        override fun operate(value1: Long, value2: Double): Double = value1 % value2
        override fun operate(value1: String, value2: String): String = notPossible("String", "String")
        override fun operate(value1: String, value2: Int): String = notPossible("String", "Int")
        override fun operate(value1: String, value2: Double): String = notPossible("String", "Double")
        override fun operate(value1: String, value2: Char): String = notPossible("String", "Char")
        override fun operate(value1: Char, value2: Char): Int = notPossible("Char", "Char")
        override fun operate(value1: Char, value2: Int): Char = notPossible("Char", "Int")
        override fun operate(value1: Char, value2: String): String = notPossible("Char", "String")
    };

    fun <T> notPossible(value1: String, value2: String): T {
        throw IllegalStateException("operation : $char is not possible between $value1 and $value2")
    }

    abstract fun operate(value1: Int, value2: Int): Int
    abstract fun operate(value1: Int, value2: Double): Double
    abstract fun operate(value1: Int, value2: Long): Long
    abstract fun operate(value1: Double, value2: Double): Double
    abstract fun operate(value1: Double, value2: Int): Double
    abstract fun operate(value1: Double, value2: Long): Double
    abstract fun operate(value1: Long, value2: Long): Long
    abstract fun operate(value1: Long, value2: Int): Long
    abstract fun operate(value1: Long, value2: Double): Double
    abstract fun operate(value1: String, value2: String): String
    abstract fun operate(value1: String, value2: Int): String
    abstract fun operate(value1: String, value2: Double): String
    abstract fun operate(value1: String, value2: Char): String
    abstract fun operate(value1: Char, value2: Char): Int
    abstract fun operate(value1: Char, value2: Int): Char
    abstract fun operate(value1: Char, value2: String): String

}

internal fun SourceStream.parseArithmeticOperator(): ArithmeticOperatorType? {
    val result = when (currentChar) {
        '+' -> ArithmeticOperatorType.Plus
        '-' -> ArithmeticOperatorType.Minus
        '/' -> ArithmeticOperatorType.Divide
        '*' -> ArithmeticOperatorType.Multiply
        '%' -> ArithmeticOperatorType.Mod
        else -> null
    }
    if (result != null) incrementPointer()
    return result
}

private class ValueAndOperatorStack {

    private val container = mutableListOf<Any>()

    fun isEmpty(): Boolean = container.isEmpty()

    fun putAllInto(other: ValueAndOperatorStack) {
        for (i in container.size - 1 downTo 0) other.container.add(container[i])
    }

    fun putValue(value: ReferencedValue) {
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

    fun peakValue(): ReferencedValue? {
        return container.lastOrNull()?.let { it as? ReferencedValue }
    }

    fun peakChar(): Char? {
        return container.lastOrNull()?.let { it as? Char }
    }

    fun popOperator(): ArithmeticOperatorType {
        return container.removeLast() as ArithmeticOperatorType
    }

    fun popValue(): ReferencedValue {
        return container.removeLast() as ReferencedValue
    }

    fun popChar(): Char {
        return container.removeLast() as Char
    }

    fun toExpression(): ExpressionValue? {
        val stack = ValueAndOperatorStack()
        while (container.isNotEmpty()) {
            when (val item = container.removeFirst()) {
                is ArithmeticOperatorType -> {
                    val second = stack.container.removeLast() as ReferencedValue
                    val first = stack.container.removeLast() as ReferencedValue
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

                is ReferencedValue -> {
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
    valueParser: ExpressionValueParser,
    allowAtLessExpressions: Boolean
): Pair<ReferencedValue, ArithmeticOperatorType?>? {
    val firstValue = with(source) { with(valueParser) { parseExpressionValue() } }
    if (firstValue != null) {
        val pointerAfterFirstValue = source.pointer
        source.increment(' ')
        return if ((source.increment('@') || (allowAtLessExpressions && !source.hasEnded))) {
            val arithmeticOperator = source.parseArithmeticOperator()
            if (arithmeticOperator != null) {
                source.increment(' ')
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

private fun SourceStream.parseExpressionWith(
    valueParser: ExpressionValueParser,
    allowAtLessExpressions: Boolean,
    stack: ValueAndOperatorStack,
    final: ValueAndOperatorStack
) {
    while (!hasEnded) {
        val valueAndOp = parseValueAndOperator(
            valueParser = valueParser,
            allowAtLessExpressions = allowAtLessExpressions
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
            if (currentChar == '(') {
                stack.putCharacter(currentChar)
                incrementPointer()
            } else if (currentChar == ')') {
                var found = false
                while (!found) {
                    if (stack.peakOperator() != null) {
                        final.putOperator(stack.popOperator())
                    } else if (stack.peakChar() != null) {
                        if (stack.peakChar() == '(') found = true
                        final.putCharacter(stack.popChar())
                    }
                }
                incrementPointer()
            } else {
                break
            }
        }
    }
    stack.putAllInto(final)
}

internal fun LazyBlock.parseExpression(
    parseFirstStringOrChar: Boolean,
    parseNotFirstStringOrChar: Boolean,
    allowAtLessExpressions: Boolean,
    parseDirectRefs: Boolean
): ReferencedValue? = parseExpression(
    firstValueParser = DefaultExpressionValueParser(
        parseStringAndChar = parseFirstStringOrChar,
        parseDirectRefs = parseDirectRefs
    ),
    notFirstValueParser = {
        DefaultExpressionValueParser(
            parseStringAndChar = parseNotFirstStringOrChar,
            parseDirectRefs = parseDirectRefs
        )
    },
    allowAtLessExpressions = allowAtLessExpressions,
)

internal fun LazyBlock.parseExpression(
    firstValueParser: ExpressionValueParser,
    notFirstValueParser: () -> ExpressionValueParser,
    allowAtLessExpressions: Boolean,
): ReferencedValue? {
    val valueAndOp = parseValueAndOperator(
        valueParser = firstValueParser,
        allowAtLessExpressions = allowAtLessExpressions
    )
    if (valueAndOp != null) {
        return if (valueAndOp.second != null) {

            val stack = ValueAndOperatorStack()
            stack.putOperator(valueAndOp.second!!)
            val final = ValueAndOperatorStack()
            final.putValue(valueAndOp.first)
            source.parseExpressionWith(
                valueParser = notFirstValueParser(),
                allowAtLessExpressions = allowAtLessExpressions,
                stack = stack,
                final = final
            )
            final.toExpression()

        } else {
            valueAndOp.first
        }
    }
    return null
}

internal fun SourceStream.parseAnyExpressionOrValue(
    parseFirstStringOrChar: Boolean = true,
    parseNotFirstStringOrChar: Boolean = true,
    parseDirectRefs: Boolean = false,
    allowAtLessExpressions: Boolean = false
): ReferencedValue? {
    parseListDefinition()?.let { return it }
    parseMutableListDefinition()?.let { return it }
    parseBooleanValue()?.let { return it }
    parseExpression(
        parseFirstStringOrChar = parseFirstStringOrChar,
        parseNotFirstStringOrChar = parseNotFirstStringOrChar,
        parseDirectRefs = parseDirectRefs,
        allowAtLessExpressions = allowAtLessExpressions
    )?.let { return it }
    return null
}