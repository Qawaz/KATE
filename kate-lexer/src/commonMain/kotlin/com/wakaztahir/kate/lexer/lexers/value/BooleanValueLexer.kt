package com.wakaztahir.kate.lexer.lexers.value

import com.wakaztahir.kate.lexer.model.DynamicTokenLexer
import com.wakaztahir.kate.lexer.stream.SourceStream
import com.wakaztahir.kate.lexer.stream.increment
import com.wakaztahir.kate.lexer.tokens.dynamic.PrimitiveToken

object BooleanValueLexer : DynamicTokenLexer<PrimitiveToken.BooleanToken> {
    override fun lex(stream: SourceStream): PrimitiveToken.BooleanToken? = with(stream) {
        if (increment("true")) return PrimitiveToken.BooleanToken(true)
        if (increment("false")) return PrimitiveToken.BooleanToken(false)
        return null
    }
}