package com.wakaztahir.kate.lexer.lexers.value

import com.wakaztahir.kate.lexer.model.DynamicToken
import com.wakaztahir.kate.lexer.model.DynamicTokenLexer
import com.wakaztahir.kate.lexer.stream.SourceStream
import com.wakaztahir.kate.lexer.tokens.dynamic.PrimitiveToken

object PrimitiveValueLexer : DynamicTokenLexer<PrimitiveToken> {
    override fun lex(stream: SourceStream): PrimitiveToken? {
        StringValueLexer.lex(stream)?.let { return it }
        CharValueLexer.lex(stream)?.let { return it }
        BooleanValueLexer.lex(stream)?.let { return it }
        NumberValueLexer.lex(stream)?.let { return it }
        return null
    }
}