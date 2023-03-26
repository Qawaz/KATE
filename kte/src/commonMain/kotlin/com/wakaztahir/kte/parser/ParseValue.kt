package com.wakaztahir.kte.parser

import com.wakaztahir.kte.model.*
import com.wakaztahir.kte.parser.stream.*
import com.wakaztahir.kte.parser.stream.increment

fun LazyBlock.parseNumberValue(): PrimitiveValue<*>? {

    var textValue = ""

    if (source.increment('-')) {
        textValue += "-"
    }

    while (!source.hasEnded && source.currentChar.isDigit()) {
        textValue += source.currentChar
        source.incrementPointer()
    }

    return if (source.increment('.')) {
        textValue += '.'
        while (!source.hasEnded && source.currentChar.isDigit()) {
            textValue += source.currentChar
            source.incrementPointer()
        }
        textValue.toDoubleOrNull()?.let { DoubleValue(it) }
    } else {
        textValue.toIntOrNull()?.let { IntValue(it) }
    } ?: run {
        if (textValue == "." || textValue == "-") source.decrementPointer()
        null
    }

}

internal fun LazyBlock.parseBooleanValue(): PrimitiveValue<*>? {
    if (source.increment("true")) return BooleanValue(true)
    if (source.increment("false")) return BooleanValue(false)
    return null
}

fun LazyBlock.parsePrimitiveValue(): PrimitiveValue<*>? {

    // Booleans
    parseBooleanValue()?.let { return it }

    // Floats & Ints
    parseNumberValue()?.let { return it }

    // Strings
    parseStringValue()?.let { return it }

    return null

}

internal fun LazyBlock.parseStringValue(): StringValue? {
    if (source.currentChar == '\"' && source.increment('\"')) {
        val value = StringValue(source.parseTextWhile { currentChar != '\"' })
        source.increment('\"')
        return value
    }
    return null
}