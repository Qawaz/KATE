package com.wakaztahir.kate.lexer.lexers

import com.wakaztahir.kate.lexer.lexers.general.TokenLexersGroup
import com.wakaztahir.kate.lexer.lexers.nodes.EmbeddingToken
import com.wakaztahir.kate.lexer.model.DynamicToken
import com.wakaztahir.kate.lexer.model.DynamicTokenLexer
import com.wakaztahir.kate.lexer.model.StaticTokenLexer
import com.wakaztahir.kate.lexer.stream.SourceStream
import com.wakaztahir.kate.lexer.stream.parseTextWhile
import com.wakaztahir.kate.lexer.tokens.StaticTokens
import com.wakaztahir.kate.lexer.tokens.dynamic.ErrorToken
import com.wakaztahir.kate.lexer.tokens.dynamic.PrimitiveToken

class EmbeddingLexer(override val source: SourceStream) : TokenLexersGroup(
    source = source,
    lexers = listOf(
        LexerFactory.New { scope ->
            StaticTokenLexer.Directive(
                token = StaticTokens.Embed,
                onNotFound = {
                    scope.stopLexing()
                    null
                }
            )
        },
        LexerFactory.New {
            StaticTokenLexer.String(
                token = StaticTokens.UnderscoreOnce,
                onNotFound = { null }
            )
        },
        LexerFactory.New { scope ->
            StaticTokenLexer.Char(
                token = StaticTokens.SingleSpace,
                onNotFound = {
                    scope.stopLexingWithError(IllegalStateException("there must be a space between @embed / @embed_once and its path"))
                    null
                }
            )
        },
        LexerFactory.New { Value }
    )
) {
    object Value : DynamicTokenLexer<DynamicToken> {
        override fun lex(stream: SourceStream): DynamicToken {
            return stream.parseTextWhile { currentChar != '\n' }.ifEmpty {
                return ErrorToken(IllegalStateException("@embed path cannot be empty"), stream)
            }.let {
                PrimitiveToken.StringToken(it.trim().replace("\n", ""))
            }
        }
    }

    fun lexEmbeddingOrThrow(): EmbeddingToken? {
        return lexTokens()?.let {
            val token = it[3] ?: return null
            if (token is PrimitiveToken.StringToken) {
                EmbeddingToken(
                    path = token.text,
                    embedOnce = it[1]?.let { true } ?: false
                )
            } else {
                (token as ErrorToken).throwError()
            }
        }
    }
}