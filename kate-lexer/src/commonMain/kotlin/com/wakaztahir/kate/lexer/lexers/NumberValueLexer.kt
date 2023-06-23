package com.wakaztahir.kate.lexer.lexers

import com.wakaztahir.kate.lexer.model.DynamicTokenLexer
import com.wakaztahir.kate.lexer.stream.SourceStream
import com.wakaztahir.kate.lexer.stream.increment
import com.wakaztahir.kate.lexer.tokens.StaticTokens
import com.wakaztahir.kate.lexer.tokens.dynamic.PrimitiveToken

object NumberValueLexer : DynamicTokenLexer<PrimitiveToken.NumberToken> {

    override fun lex(stream: SourceStream): PrimitiveToken.NumberToken? = with(stream) {
        return@with lookAhead {

            var textValue = ""

            if (increment('-')) {
                textValue += '-'
            }

            while (!hasEnded && currentChar.isDigit()) {
                textValue += currentChar
                incrementPointer()
            }

            if (increment('.')) {
                textValue += '.'
                while (!hasEnded && currentChar.isDigit()) {
                    textValue += currentChar
                    incrementPointer()
                }
                textValue.toDoubleOrNull()?.let { PrimitiveToken.NumberToken.DoubleToken(it) }
            } else {
                if (increment(StaticTokens.LongEnder)) {
                    textValue.toLongOrNull()?.let { PrimitiveToken.NumberToken.LongToken(it) } ?: run {
                        throw IllegalStateException("")
                    }
                } else {
                    textValue.toIntOrNull()?.let { PrimitiveToken.NumberToken.IntToken(it) }
                }
            } ?: run {
                if (textValue == "." || textValue == "-") {
                    restorePosition()
                }
                null
            }
        }
    }

}