package com.wakaztahir.kte.parser

import com.wakaztahir.kte.model.*
import com.wakaztahir.kte.parser.stream.*
import com.wakaztahir.kte.parser.stream.increment

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
        textValue.toFloatOrNull()?.let { FloatValue(it) }
    } else {
        textValue.toIntOrNull()?.let { IntValue(it) }
    }

}

fun SourceStream.parsePrimitiveValue(): PrimitiveValue<*>? {

    // Booleans
    if (increment("true")) return BooleanValue(true)
    if (increment("false")) return BooleanValue(false)

    // Floats & Ints
    parseNumberValue()?.let { return it }

    // Strings
    parseStringValue()?.let { return it }

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