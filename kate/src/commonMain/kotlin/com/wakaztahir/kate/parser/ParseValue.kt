package com.wakaztahir.kate.parser

import com.wakaztahir.kate.model.*
import com.wakaztahir.kate.parser.stream.*
import com.wakaztahir.kate.parser.stream.increment

fun SourceStream.parseNumberValue(): PrimitiveValue<*>? {

    var textValue = ""

    if (increment('-')) {
        textValue += "-"
    }

    while (!hasEnded && currentChar.isDigit()) {
        textValue += currentChar
        incrementPointer()
    }

    return if (increment('.')) {
        textValue += '.'
        while (!hasEnded && currentChar.isDigit()) {
            textValue += currentChar
            incrementPointer()
        }
        textValue.toDoubleOrNull()?.let { DoubleValue(it) }
    } else {
        if (increment('L')) {
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

internal fun SourceStream.parseBooleanValue(): PrimitiveValue<*>? {
    if (increment("true")) return BooleanValue(true)
    if (increment("false")) return BooleanValue(false)
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

internal fun SourceStream.parseCharacterValue(): CharValue? {
    if (currentChar == '\'' && increment('\'')) {
        val characterValue = if (currentChar == '\\') {
            incrementPointer()
            currentChar.transformAfterBackslashChar()
                ?: throw IllegalArgumentException("unknown character after backslash $currentChar")
        } else {
            currentChar
        }
        val value = CharValue(characterValue)
        incrementPointer()
        if (!increment('\'')) {
            throw IllegalStateException("a char value must end with '")
        }
        return value
    }
    return null
}

internal fun SourceStream.parseStringValue(): StringValue? {
    if (currentChar == '\"' && increment('\"')) {
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
        if (!increment('\"')) {
            throw IllegalArgumentException("string value must end with \" value : $value")
        }
        return StringValue(value)
    }
    return null
}