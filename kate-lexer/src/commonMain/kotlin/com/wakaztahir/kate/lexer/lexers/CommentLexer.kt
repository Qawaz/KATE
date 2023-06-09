package com.wakaztahir.kate.lexer.lexers

import com.wakaztahir.kate.lexer.lexers.general.TokenLexersGroup
import com.wakaztahir.kate.lexer.model.DynamicTokenLexer
import com.wakaztahir.kate.lexer.model.StaticTokenLexer
import com.wakaztahir.kate.lexer.stream.SourceStream
import com.wakaztahir.kate.lexer.stream.parseTextUntil
import com.wakaztahir.kate.lexer.tokens.StaticTokens
import com.wakaztahir.kate.lexer.tokens.dynamic.PrimitiveToken

class CommentLexer(source: SourceStream) : TokenLexersGroup(
    source = source,
    lexers = listOf(
        LexerFactory.New { scope ->
            StaticTokenLexer.String(
                token = StaticTokens.CommentStart,
                onNotFound = {
                    scope.stopLexing()
                    null
                }
            )
        },
        LexerFactory.New { Value },
        LexerFactory.New { scope ->
            StaticTokenLexer.String(
                token = StaticTokens.CommentEnd,
                onNotFound = {
                    scope.stopLexingWithError(CommentLexException("comment must end with ${StaticTokens.CommentEnd}"))
                    null
                }
            )
        },
    )
) {

    class CommentLexException(message: String) : Exception(message)

    object Value : DynamicTokenLexer<PrimitiveToken.StringToken> {
        override fun lex(stream: SourceStream): PrimitiveToken.StringToken? = with(stream) {
            val commentText = parseTextUntil(StaticTokens.CommentEnd)
            return@with commentText?.let { PrimitiveToken.StringToken(it) }
        }
    }

    fun lexCommentText(): String? {
        return lexTokens()?.let { (it[1] as? PrimitiveToken.StringToken)?.text }
    }

}