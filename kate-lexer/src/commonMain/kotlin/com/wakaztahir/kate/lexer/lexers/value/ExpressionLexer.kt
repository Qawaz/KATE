package com.wakaztahir.kate.lexer.lexers.value

import com.wakaztahir.kate.lexer.model.ValueLexer
import com.wakaztahir.kate.lexer.stream.SourceStream
import com.wakaztahir.kate.lexer.stream.increment
import com.wakaztahir.kate.lexer.tokens.StaticTokens
import com.wakaztahir.kate.lexer.tokens.dynamic.ExpressionToken
import com.wakaztahir.kate.lexer.tokens.dynamic.ValueToken
import com.wakaztahir.kate.model.expression.ArithmeticOperatorType
import com.wakaztahir.kate.model.expression.OperatorAssociativity

class ExpressionLexer(val valueLexer: ValueLexer<ValueToken>) : ValueLexer<ValueToken> {

    private class ValueAndOperatorStack {

        private val container = mutableListOf<Any>()

        fun isEmpty(): Boolean = container.isEmpty()

        fun putAllInto(other: ValueAndOperatorStack) {
            for (i in container.size - 1 downTo 0) other.container.add(container[i])
        }

        fun putValue(value: ValueToken) {
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

        fun peakValue(): ValueToken? {
            return container.lastOrNull()?.let { it as? ValueToken }
        }

        fun peakChar(): Char? {
            return container.lastOrNull()?.let { it as? Char }
        }

        fun popOperator(): ArithmeticOperatorType {
            return container.removeLast() as ArithmeticOperatorType
        }

        fun popValue(): ValueToken {
            return container.removeLast() as ValueToken
        }

        fun popChar(): Char {
            return container.removeLast() as Char
        }

        fun toExpression(): ExpressionToken? {
            val stack = ValueAndOperatorStack()
            while (container.isNotEmpty()) {
                when (val item = container.removeFirst()) {
                    is ArithmeticOperatorType -> {
                        val second = stack.container.removeLast() as ValueToken
                        val first = stack.container.removeLast() as ValueToken
                        stack.putValue(
                            ExpressionToken(
                                first = first,
                                operator = item,
                                second = second,
                            )
                        )
                    }

                    is Char -> {

                    }

                    is ValueToken -> {
                        stack.putValue(item)
                    }

                    else -> {
                        throw IllegalStateException("Unknown value type found in stack")
                    }
                }
            }
            return stack.peakValue()?.let { it as? ExpressionToken }//?.also {
            //println("Expression : $it")
            //}
        }

    }

    internal fun SourceStream.parseArithmeticOperator(): ArithmeticOperatorType? {
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
                if (increment(com.wakaztahir.kate.lexer.tokens.StaticTokens.AndOperator)) ArithmeticOperatorType.And else null
            }

            '|' -> {
                if (increment(com.wakaztahir.kate.lexer.tokens.StaticTokens.OrOperator)) ArithmeticOperatorType.Or else null
            }
            // Conditional Operators
            '!' -> {
                return null
                if (increment(com.wakaztahir.kate.lexer.tokens.StaticTokens.NotEqual)) ArithmeticOperatorType.NotEqual else null
            }

            '=' -> {
                return null
                if (increment(com.wakaztahir.kate.lexer.tokens.StaticTokens.Equals)) {
                    if (increment(com.wakaztahir.kate.lexer.tokens.StaticTokens.SingleEqual)) ArithmeticOperatorType.ReferentialEquality else ArithmeticOperatorType.Equal
                } else null
            }

            '>' -> {
                return null
                if (increment(com.wakaztahir.kate.lexer.tokens.StaticTokens.GreaterThanOrEqualTo)) {
                    ArithmeticOperatorType.GreaterThanOrEqual
                } else ArithmeticOperatorType.GreaterThan
            }

            '<' -> {
                return null
                if (increment(com.wakaztahir.kate.lexer.tokens.StaticTokens.LessThanOrEqualTo)) ArithmeticOperatorType.LessThanOrEqual else ArithmeticOperatorType.LessThan
            }

            else -> null
        }
        return result
    }

    private fun SourceStream.parseExpressionWith(
        stack: ValueAndOperatorStack,
        final: ValueAndOperatorStack
    ) {
        while (!hasEnded) {
            val valueAndOp = parseValueAndOperator()
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

    private fun SourceStream.parseValueAndOperator(): Pair<ValueToken, ArithmeticOperatorType?>? {
        val source = this
        val firstValue = valueLexer.lex(this)
        if (firstValue != null) {
            return lookAhead {
                source.increment(StaticTokens.SingleSpace)
                if (!source.hasEnded) {
                    val arithmeticOperator = source.parseArithmeticOperator()
                    if (arithmeticOperator != null) {
                        source.increment(StaticTokens.SingleSpace)
                        Pair(firstValue, arithmeticOperator)
                    } else {
                        restorePosition()
                        Pair(firstValue, null)
                    }
                } else {
                    restorePosition()
                    Pair(firstValue, null)
                }
            }
        }
        return null
    }

    internal fun SourceStream.parseExpressionAfter(
        value: ValueToken,
        operator: ArithmeticOperatorType
    ): ExpressionToken? {
        val stack = ValueAndOperatorStack()
        stack.putOperator(operator)
        val final = ValueAndOperatorStack()
        final.putValue(value)
        parseExpressionWith(
            stack = stack,
            final = final
        )
        return final.toExpression()
    }

    private fun SourceStream.parseExpression(): ValueToken? {
        val valueAndOp = parseValueAndOperator()
        if (valueAndOp != null) {
            return if (valueAndOp.second != null) {
                return parseExpressionAfter(
                    value = valueAndOp.first,
                    operator = valueAndOp.second!!
                )
            } else {
                valueAndOp.first
            }
        }
        return null
    }

    override fun lex(stream: SourceStream): ValueToken? {
        return stream.parseExpression()
    }
}