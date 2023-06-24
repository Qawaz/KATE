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

class RuntimeWriteLexer(source: SourceStream) : TokenLexersGroup(
    source = source,
    lexers = listOf(
        LexerFactory.New { scope ->
            StaticTokenLexer.Directive(
                token = StaticTokens.RuntimeWrite,
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
                    scope.stopLexingWithError(IllegalStateException("left parenthesis '(' expected after @write"))
                    null
                }
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

    fun lexWriteValueToken(): ValueToken? {
        return lexTokens()?.let { list ->
            list[2]?.let { it as ValueToken }
        }
    }

}