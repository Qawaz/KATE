package com.wakaztahir.kate.lexer.lexers

import com.wakaztahir.kate.lexer.lexers.general.TokenLexersGroup
import com.wakaztahir.kate.lexer.lexers.nodes.PlaceholderCall
import com.wakaztahir.kate.lexer.lexers.value.DefaultExpressionValueLexer
import com.wakaztahir.kate.lexer.lexers.value.ExpressionLexer
import com.wakaztahir.kate.lexer.model.DynamicToken
import com.wakaztahir.kate.lexer.model.DynamicTokenLexer
import com.wakaztahir.kate.lexer.model.StaticTokenLexer
import com.wakaztahir.kate.lexer.stream.SourceStream
import com.wakaztahir.kate.lexer.stream.parseTextWhile
import com.wakaztahir.kate.lexer.tokens.StaticTokens
import com.wakaztahir.kate.lexer.tokens.dynamic.ErrorToken
import com.wakaztahir.kate.lexer.tokens.dynamic.PrimitiveToken
import com.wakaztahir.kate.lexer.tokens.dynamic.ValueToken

class RuntimeGenLexer(source: SourceStream) : TokenLexersGroup(
    source = source,
    lexers = listOf(
        LexerFactory.New { scope ->
            StaticTokenLexer.Directive(
                token = StaticTokens.PlaceholderCall,
                onNotFound = {
                    scope.stopLexing()
                    null
                }
            )
        },
        LexerFactory.New { scope ->
            StaticTokenLexer.Char(
                token = StaticTokens.LeftParenthesis,
                onNotFound = {
                    scope.stopLexingWithError(IllegalStateException("placeholder name is required when invoking a placeholder using @placeholder"))
                    null
                }
            )
        },
        LexerFactory.New { NameLexer(isRequired = true) },
        LexerFactory.New {
            StaticTokenLexer.Char(
                token = StaticTokens.Comma,
                onNotFound = { null }
            )
        },
        LexerFactory.New { NameLexer(isRequired = false) },
        LexerFactory.New {
            StaticTokenLexer.Char(
                token = StaticTokens.Comma,
                onNotFound = { null }
            )
        },
        LexerFactory.New { ExpressionLexer(DefaultExpressionValueLexer(parseDirectRefs = true)) },
        LexerFactory.New {
            StaticTokenLexer.Char(
                token = StaticTokens.RightParenthesis,
                onNotFound = { null }
            )
        },
    )
) {
    class NameLexer(private val isRequired: Boolean) : DynamicTokenLexer<DynamicToken> {
        private fun Char.isPlaceholderName() = this.isLetterOrDigit() || this == '_'
        override fun lex(stream: SourceStream): DynamicToken? {
            stream.parseTextWhile { currentChar.isPlaceholderName() }.ifEmpty {
                if (isRequired) {
                    return ErrorToken(IllegalStateException("placeholder name cannot be empty"), stream)
                } else {
                    return null
                }
            }.let {
                return PrimitiveToken.StringToken(it)
            }
        }
    }

    fun lexPlaceholderToken(): PlaceholderCall? {
        return lexTokens()?.let { list ->
            val name = list[2]?.let { it as PrimitiveToken.StringToken } ?: return null
            val definitionName = list[4]?.let { it as PrimitiveToken.StringToken }
            val paramToken = list[6]?.let { it as ValueToken }
            PlaceholderCall(
                name = name.text,
                definitionName = definitionName?.text,
                param = paramToken
            )
        }
    }

}