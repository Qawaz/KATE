package com.wakaztahir.kte.parser

import com.wakaztahir.kte.model.LazyBlock
import com.wakaztahir.kte.model.model.KTEListImpl
import com.wakaztahir.kte.model.model.KTEMutableListImpl
import com.wakaztahir.kte.model.model.ReferencedValue
import com.wakaztahir.kte.parser.stream.increment

private fun LazyBlock.parseListParameters(list: MutableList<ReferencedValue> = mutableListOf()): MutableList<ReferencedValue> {
    do {
        val value = source.parseAnyExpressionOrValue()
        if (value != null) {
            list.add(value)
        } else {
            throw IllegalStateException("expected a referenced value in list parameters")
        }
    } while (source.increment(','))
    if (source.increment(')')) {
        return list
    } else {
        throw IllegalStateException("expected ')' when defining list , got ${source.currentChar}")
    }
}

fun LazyBlock.parseListDefinition(): ReferencedValue? {
    if (source.currentChar == '@' && source.increment("@list(")) {
        val parameters = parseListParameters()
        return KTEListImpl(parameters.toList())
    }
    return null
}

fun LazyBlock.parseMutableListDefinition(): ReferencedValue? {
    if (source.currentChar == '@' && source.increment("@mutable_list(")) {
        val parameters = parseListParameters()
        return KTEMutableListImpl(parameters)
    }
    return null
}