package com.wakaztahir.kate.lexer.lexers

import com.wakaztahir.kate.lexer.lexers.general.TokenLexersGroup
import com.wakaztahir.kate.lexer.lexers.nodes.PlaceholderUse
import com.wakaztahir.kate.lexer.lexers.nodes.VariableAssignment
import com.wakaztahir.kate.lexer.lexers.value.ArithmeticOperatorLexer
import com.wakaztahir.kate.lexer.lexers.value.DefaultExpressionValueLexer
import com.wakaztahir.kate.lexer.lexers.value.ExpressionLexer
import com.wakaztahir.kate.lexer.model.ArithmeticOperatorToken
import com.wakaztahir.kate.lexer.model.DynamicToken
import com.wakaztahir.kate.lexer.model.DynamicTokenLexer
import com.wakaztahir.kate.lexer.model.StaticTokenLexer
import com.wakaztahir.kate.lexer.stream.SourceStream
import com.wakaztahir.kate.lexer.stream.parseTextWhile
import com.wakaztahir.kate.lexer.stream.printLeft
import com.wakaztahir.kate.lexer.tokens.StaticTokens
import com.wakaztahir.kate.lexer.tokens.dynamic.ErrorToken
import com.wakaztahir.kate.lexer.tokens.dynamic.PrimitiveToken
import com.wakaztahir.kate.lexer.tokens.dynamic.ValueToken

class VariableAssignmentLexer(source: SourceStream, val isDefaultNoRaw: Boolean) : TokenLexersGroup(
    source = source,
    lexers = listOf(
        LexerFactory.New { scope ->
            StaticTokenLexer.Directive(
                token = StaticTokens.SetVar,
                onNotFound = {
                    if (isDefaultNoRaw) {
                        scope.stopLexing()
                    }
                    null
                }
            )
        },
        LexerFactory.New { StaticTokenLexer.Whitespace },
        LexerFactory.New { NameLexer },
        LexerFactory.New { StaticTokenLexer.Whitespace },
        LexerFactory.New { ArithmeticOperatorLexer },
        LexerFactory.New { scope ->
            StaticTokenLexer.Char(
                token = StaticTokens.SingleEqual,
                onNotFound = {
                    if (isDefaultNoRaw) {
                        scope.stopLexingWithError(IllegalStateException("expected '${StaticTokens.SingleEqual}' after left hand side of the assignment expression"))
                    }
                    null
                }
            )
        },
        LexerFactory.New { StaticTokenLexer.Whitespace },
        LexerFactory.New { ExpressionLexer(DefaultExpressionValueLexer(parseDirectRefs = true)) },
    )
) {

    object NameLexer : DynamicTokenLexer<DynamicToken> {

        private fun isLanguageKeyword(varName: String): Boolean {
            return when (varName) {
                "this" -> true
                "parent" -> true
                else -> false
            }
        }

        private fun isValidVariableName(name: String): Result<Boolean> {
            if (name.isEmpty()) {
                return Result.failure(Throwable("variable name cannot be empty"))
            }
            if (name[0].isDigit()) {
                return Result.failure(Throwable("variable name cannot begin with a digit"))
            }
            if (isLanguageKeyword(name)) {
                return Result.failure(Throwable("variable name \"$name\" is a language keyword"))
            }
            return Result.success(true)
        }

        private fun Char.isVariableName(): Boolean = this.isLetterOrDigit() || this == '_'

        override fun lex(stream: SourceStream): DynamicToken? {
            stream.parseTextWhile { currentChar.isVariableName() }.let {
                if(it.isEmpty()) return null
                val result = isValidVariableName(it)
                if (result.isSuccess) {
                    return PrimitiveToken.StringToken(it)
                } else {
                    return ErrorToken(result.exceptionOrNull()!!, stream = stream)
                }
            }
        }
    }

    fun lexVariableAssignment(): VariableAssignment? {
        return lexTokens()?.let { list ->
            val name = list[2]?.let { it as PrimitiveToken.StringToken } ?: return null
            val arithmeticOperatorToken = list[4]?.let { it as ArithmeticOperatorToken }
            val value = list[7]?.let { it as ValueToken }
            println(list)
            VariableAssignment(
                variableName = name.text,
                arithmeticOperatorType = arithmeticOperatorToken?.operator,
                variableValue = value!!
            )
        }
    }

}