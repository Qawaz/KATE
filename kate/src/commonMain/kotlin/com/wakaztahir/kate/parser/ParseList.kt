package com.wakaztahir.kate.parser

import com.wakaztahir.kate.model.KATEType
import com.wakaztahir.kate.model.LazyBlock
import com.wakaztahir.kate.model.model.*
import com.wakaztahir.kate.parser.stream.increment
import com.wakaztahir.kate.parser.variable.parseValueOfType

private fun KATEType.actual() = if (this is KATEType.NullableKateType) this.actual else this

private fun comparedNullable(first: KATEType, other: KATEType): KATEType? {
    if (first !is KATEType.NullableKateType && other !is KATEType.NullableKateType && first == other) return first
    if (first.actual() == other.actual()) {
        return if (first is KATEType.NullableKateType) first else other
    }
    return null
}

private fun List<ReferencedOrDirectValue>.inferItemType(): KATEType? {
    var type: KATEType? = null
    for (value in this) {
        // todo not checking unknown type
        val valueType = (if (value is KATEValue) value.getKnownKATEType() else null) ?: continue
        type = if (type == null) {
            valueType
        } else {
            comparedNullable(type, valueType) ?: return null
        }
    }
    return type
}

private fun LazyBlock.parseListParameters(
    list: MutableList<ReferencedOrDirectValue> = mutableListOf(),
    itemType: KATEType,
    allowEmpty: Boolean
): MutableList<ReferencedOrDirectValue> {
    do {
        val value = parseValueOfType(
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

class ListOfReferencedOrDirectValues(
    private val parameters: MutableList<ReferencedOrDirectValue>,
    private val itemType: KATEType,
    private val isMutable: Boolean
) : ReferencedOrDirectValue {

    var value: KATEList<KATEValue>? = null
        private set

    // todo not at all fast
    override fun getKATEValue(): KATEValue {
        if (value == null) {
            val params = parameters.map { it.getKATEValueAndType() }
            value = if (isMutable) KATEMutableListImpl(
                collection = params.map { it.first }.toMutableList(),
                itemType = itemType
            ) else {
                KATEListImpl(
                    collection = params.map { it.first },
                    itemType = itemType
                )
            }
            value!!.apply {
                params.forEachIndexed { index, pair -> pair.second?.let { setExplicitType(index, it) } }
            }
        }
        return value!!
    }

    override fun getKATEValueAndType(): Pair<KATEValue, KATEType?> {
        return getKATEValue().let { Pair(it, it.getKnownKATEType()) }
    }

}

fun LazyBlock.parseListDefinition(itemType: KATEType? = null): ReferencedOrDirectValue? {
    if (source.currentChar == '@' && source.increment("@list")) {
        if (source.increment('(')) {
            val parameters = parseListParameters(allowEmpty = true, itemType = itemType ?: KATEType.Any)
            return ListOfReferencedOrDirectValues(
                parameters = parameters,
                itemType = itemType ?: parameters.inferItemType() ?: KATEType.Any,
                isMutable = false
            )
        } else {
            throw IllegalStateException("expected '(' got ${source.currentChar}")
        }
    }
    return null
}

fun LazyBlock.parseMutableListDefinition(itemType: KATEType? = null): ReferencedOrDirectValue? {
    if (source.currentChar == '@' && source.increment("@mutable_list")) {
        if (source.increment('(')) {
            val parameters = parseListParameters(allowEmpty = true, itemType = itemType ?: KATEType.Any)
            return ListOfReferencedOrDirectValues(
                parameters = parameters,
                itemType = itemType ?: parameters.inferItemType() ?: KATEType.Any,
                isMutable = true
            )
        } else {
            throw IllegalStateException("expected '(' got ${source.currentChar}")
        }
    }
    return null
}