package com.wakaztahir.kate.lexer.lexers.value

import com.wakaztahir.kate.lexer.model.ArithmeticOperatorToken
import com.wakaztahir.kate.lexer.model.DynamicTokenLexer
import com.wakaztahir.kate.lexer.stream.SourceStream
import com.wakaztahir.kate.lexer.stream.increment
import com.wakaztahir.kate.lexer.stream.parseTextWhile
import com.wakaztahir.kate.model.expression.ArithmeticOperatorType

object ArithmeticOperatorLexer : DynamicTokenLexer<ArithmeticOperatorToken> {
    private fun SourceStream.parseArithmeticOperator(): ArithmeticOperatorType? {
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

    override fun lex(stream: SourceStream): ArithmeticOperatorToken? {
        return stream.parseArithmeticOperator()?.let {
            ArithmeticOperatorToken(it)
        }
    }
}
