package com.wakaztahir.kate.parser

import com.wakaztahir.kate.model.KATEType
import com.wakaztahir.kate.model.LazyBlock
import com.wakaztahir.kate.model.model.KATEListImpl
import com.wakaztahir.kate.model.model.KATEMutableListImpl
import com.wakaztahir.kate.model.model.KATEValue
import com.wakaztahir.kate.parser.stream.increment
import com.wakaztahir.kate.parser.variable.parseKATEType

private fun LazyBlock.parseListParameters(
    list: MutableList<KATEValue> = mutableListOf(),
    allowEmpty: Boolean
): MutableList<KATEValue> {
    do {
        val value = source.parseAnyExpressionOrValue()
        if (value != null) {
            list.add(value)
        } else {
            if (allowEmpty) {
                break
            } else {
                throw IllegalStateException("expected a referenced value in list parameters")
            }
        }
    } while (source.increment(','))
    if (source.increment(')')) {
        return list
    } else {
        throw IllegalStateException("expected ')' when defining list , got ${source.currentChar}")
    }
}

private fun LazyBlock.parseListItemType(): KATEType? {
    return if (source.increment('<')) {
        val type = source.parseKATEType()
            ?: throw IllegalStateException("expected a type after '<' got ${source.currentChar}")
        if (!source.increment('>')) {
            throw IllegalStateException("expected '>' got ${source.currentChar}")
        }
        type
    } else {
        null
    }
}

fun LazyBlock.parseListDefinition(): KATEValue? {
    if (source.currentChar == '@' && source.increment("@list")) {
        val itemType = parseListItemType() ?: KATEType.Any()
        if (source.increment('(')) {
            val parameters = parseListParameters(allowEmpty = true)
            return KATEListImpl(collection = parameters, itemType = itemType)
        } else {
            throw IllegalStateException("expected '(' got ${source.currentChar}")
        }
    }
    return null
}

fun LazyBlock.parseMutableListDefinition(): KATEValue? {
    if (source.currentChar == '@' && source.increment("@mutable_list")) {
        val itemType = parseListItemType() ?: KATEType.Any()
        if (source.increment('(')) {
            val parameters = parseListParameters(allowEmpty = true)
            return KATEMutableListImpl(collection = parameters, itemType = itemType)
        } else {
            throw IllegalStateException("expected '(' got ${source.currentChar}")
        }
    }
    return null
}