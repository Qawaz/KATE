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

internal fun SourceStream.parseCharacterValue(): CharValue? {
    if (currentChar == '\'' && increment('\'')) {
        val value = CharValue(currentChar)
        incrementPointer()
        if (!increment('\'')) {
            throw IllegalStateException("a char ends with '")
        }
        return value
    }
    return null
}

internal fun SourceStream.parseStringValue(): StringValue? {
    if (currentChar == '\"' && increment('\"')) {
        val value = StringValue(parseTextWhile { currentChar != '\"' })
        increment('\"')
        return value
    }
    return null
}