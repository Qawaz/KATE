package com.wakaztahir.kate.parser

import com.wakaztahir.kate.lexer.lexers.value.BooleanValueLexer
import com.wakaztahir.kate.lexer.lexers.value.CharValueLexer
import com.wakaztahir.kate.lexer.lexers.value.NumberValueLexer
import com.wakaztahir.kate.lexer.lexers.value.StringValueLexer
import com.wakaztahir.kate.model.*
import com.wakaztahir.kate.lexer.tokens.dynamic.PrimitiveToken
import com.wakaztahir.kate.parser.stream.ParserSourceStream

fun ParserSourceStream.parseNumberValue(): PrimitiveValue<*>? {

    val token = NumberValueLexer.lex(this)

    return token?.let {
        when (it) {
            is PrimitiveToken.NumberToken.DoubleToken -> DoubleValue(it.value)
            is PrimitiveToken.NumberToken.IntToken -> IntValue(it.value)
            is PrimitiveToken.NumberToken.LongToken -> LongValue(it.value)
        }
    }

}

internal fun ParserSourceStream.parseBooleanValue(): PrimitiveValue<*>? {
    return BooleanValueLexer.lex(this)?.value?.let { BooleanValue(it) }
}

internal fun ParserSourceStream.parseCharacterValue(): CharValue? {
    return CharValueLexer.lex(this)?.char?.let { CharValue(it) }
}

internal fun ParserSourceStream.parseStringValue(): StringValue? {
    return StringValueLexer.lex(this)?.text?.let { StringValue(it) }
}