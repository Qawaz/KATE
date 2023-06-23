package com.wakaztahir.kate.parser

import com.wakaztahir.kate.lexer.tokens.StaticTokens
import com.wakaztahir.kate.model.*
import com.wakaztahir.kate.lexer.stream.*
import com.wakaztahir.kate.lexer.stream.increment
import com.wakaztahir.kate.parser.stream.ParserSourceStream

fun ParserSourceStream.parseNumberValue(): PrimitiveValue<*>? {

    var textValue = ""

    if (increment(StaticTokens.NegativeValueDash)) {
        textValue += "-"
    }

    while (!hasEnded && currentChar.isDigit()) {
        textValue += currentChar
        incrementPointer()
    }

    return if (increment(StaticTokens.Dot)) {
        textValue += '.'
        while (!hasEnded && currentChar.isDigit()) {
            textValue += currentChar
            incrementPointer()
        }
        textValue.toDoubleOrNull()?.let { DoubleValue(it) }
    } else {
        if (increment(StaticTokens.LongEnder)) {
            textValue.toLongOrNull()?.let { LongValue(it) } ?: run {
                decrementPointer()
                null
            }
        } else {
            textValue.toIntOrNull()?.let { IntValue(it) }
        }
    } ?: run {
        if (textValue == "." || textValue == "-") decrementPointer()
        null
    }

}

internal fun ParserSourceStream.parseBooleanValue(): PrimitiveValue<*>? {
    if (increment(StaticTokens.True)) return BooleanValue(true)
    if (increment(StaticTokens.False)) return BooleanValue(false)
    return null
}

private fun Char.transformAfterBackslashChar(): Char? {
    return when (this) {
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

internal fun ParserSourceStream.parseCharacterValue(): CharValue? {
    if (increment(StaticTokens.SingleQuote)) {
        val characterValue = if (currentChar == '\\') {
            incrementPointer()
            currentChar.transformAfterBackslashChar()
                ?: throw IllegalArgumentException("unknown character after backslash $currentChar")
        } else {
            currentChar
        }
        val value = CharValue(characterValue)
        incrementPointer()
        if (!increment(StaticTokens.SingleQuote)) {
            throw IllegalStateException("a char value must end with '")
        }
        return value
    }
    return null
}

internal fun ParserSourceStream.parseStringValue(): StringValue? {
    if (increment(StaticTokens.DoubleQuote)) {
        var value = ""
        while (!hasEnded && currentChar != '\"') {
            value += if (currentChar == '\\') {
                incrementPointer()
                currentChar.transformAfterBackslashChar()
            } else {
                currentChar
            }
            incrementPointer()
        }
        if (!increment(StaticTokens.DoubleQuote)) {
            throw IllegalArgumentException("string value must end with ${StaticTokens.DoubleQuote} value : $value")
        }
        return StringValue(value)
    }
    return null
}