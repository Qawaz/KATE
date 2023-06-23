package com.wakaztahir.kate.lexer.lexers.value

import com.wakaztahir.kate.lexer.model.DynamicTokenLexer
import com.wakaztahir.kate.lexer.stream.SourceStream
import com.wakaztahir.kate.lexer.stream.increment
import com.wakaztahir.kate.lexer.tokens.dynamic.PrimitiveToken

object StringValueLexer : DynamicTokenLexer<PrimitiveToken.StringToken> {
    override fun lex(stream: SourceStream): PrimitiveToken.StringToken? = with(stream) {
        if (increment('"')) {
            var value = ""
            while (!hasEnded && currentChar != '\"') {
                value += if (currentChar == '\\') {
                    incrementPointer()
                    CharValueLexer.transformAfterBackslashChar(currentChar)
                } else {
                    currentChar
                }
                incrementPointer()
            }
            if (!increment('"')) {
                throw IllegalArgumentException("string value must end with ${'"'} value : $value")
            }
            return@with PrimitiveToken.StringToken(value)
        }
        return null
    }
}