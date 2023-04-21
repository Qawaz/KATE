package com.wakaztahir.kate.parser

import com.wakaztahir.kate.model.KATEType
import com.wakaztahir.kate.model.LazyBlock
import com.wakaztahir.kate.model.model.KATEListImpl
import com.wakaztahir.kate.model.model.KATEMutableListImpl
import com.wakaztahir.kate.model.model.KATEValue
import com.wakaztahir.kate.model.model.ReferencedValue
import com.wakaztahir.kate.parser.stream.increment
import com.wakaztahir.kate.parser.variable.parseKATEType
import com.wakaztahir.kate.parser.variable.parseValueOfType

private fun KATEType.actual() = if (this is KATEType.NullableKateType) this.actual else this

private fun comparedNullable(first: KATEType, other: KATEType): KATEType? {
    if (first !is KATEType.NullableKateType && other !is KATEType.NullableKateType && first == other) return first
    if (first.actual() == other.actual()) {
        return if (first is KATEType.NullableKateType) first else other
    }
    return null
}

private fun List<KATEValue>.inferItemType(): KATEType? {
    var type: KATEType? = null
    for (value in this) {
        // todo not checking unknown type
        val valueType = value.getKnownKATEType() ?: continue
        type = if (type == null) {
            valueType
        } else {
            comparedNullable(type, valueType) ?: return null
        }
    }
    return type
}

private fun LazyBlock.parseListParameters(
    list: MutableList<KATEValue> = mutableListOf(),
    itemType: KATEType,
    allowEmpty: Boolean
): MutableList<KATEValue> {
    do {
        val value = source.parseValueOfType(
            type = itemType,
            allowAtLessExpressions = true,
            parseDirectRefs = true
        )
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

fun LazyBlock.parseListDefinition(itemType: KATEType? = null): KATEValue? {
    if (source.currentChar == '@' && source.increment("@list")) {
        if (source.increment('(')) {
            val parameters = parseListParameters(allowEmpty = true, itemType = itemType ?: KATEType.Any())
            return KATEListImpl(
                collection = parameters,
                itemType = itemType ?: parameters.inferItemType() ?: KATEType.Any()
            )
        } else {
            throw IllegalStateException("expected '(' got ${source.currentChar}")
        }
    }
    return null
}

fun LazyBlock.parseMutableListDefinition(itemType: KATEType? = null): KATEValue? {
    if (source.currentChar == '@' && source.increment("@mutable_list")) {
        if (source.increment('(')) {
            val parameters = parseListParameters(allowEmpty = true, itemType = itemType ?: KATEType.Any())
            return KATEMutableListImpl(
                collection = parameters,
                itemType = itemType ?: parameters.inferItemType() ?: KATEType.Any()
            )
        } else {
            throw IllegalStateException("expected '(' got ${source.currentChar}")
        }
    }
    return null
}