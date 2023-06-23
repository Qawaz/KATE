package com.wakaztahir.kate.lexer.lexers.value

import com.wakaztahir.kate.lexer.model.DynamicTokenLexer
import com.wakaztahir.kate.lexer.stream.SourceStream
import com.wakaztahir.kate.lexer.stream.increment
import com.wakaztahir.kate.lexer.tokens.StaticTokens
import com.wakaztahir.kate.lexer.tokens.dynamic.PrimitiveToken

object CharValueLexer : DynamicTokenLexer<PrimitiveToken.CharToken> {

    fun transformAfterBackslashChar(char : Char): Char? {
        return when (char) {
            'b' -> '\b'
            'n' -> '\n'
            'r' -> '\r'
            't' -> '\t'
            '\\' -> '\\'
            '\'' -> '\''
            '\"' -> '\"'
            else -> null
        }
    }

    override fun lex(stream: SourceStream): PrimitiveToken.CharToken? = with(stream) {
        if (increment(StaticTokens.SingleQuote)) {
            val characterValue = if (currentChar == '\\') {
                incrementPointer()
                transformAfterBackslashChar(currentChar)
                    ?: throw IllegalArgumentException("unknown character after backslash $currentChar")
            } else {
                currentChar
            }
            val value = PrimitiveToken.CharToken(characterValue)
            incrementPointer()
            if (!increment(StaticTokens.SingleQuote)) {
                throw IllegalStateException("a char value must end with '")
            }
            return value
        }
        return null
    }

}