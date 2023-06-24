package com.wakaztahir.kate.lexer.lexers.value

import com.wakaztahir.kate.lexer.model.ValueLexer
import com.wakaztahir.kate.lexer.stream.SourceStream
import com.wakaztahir.kate.lexer.stream.printLeft
import com.wakaztahir.kate.lexer.tokens.dynamic.ValueToken

class DefaultExpressionValueLexer(val parseDirectRefs: Boolean) : ValueLexer<ValueToken> {
    override fun lex(stream: SourceStream): ValueToken? {
        PrimitiveValueLexer.lex(stream)?.let { return it }
        AccessChainLexer(parseDirectRefs = parseDirectRefs).lex(stream)?.let { return it }
        return null
    }
}