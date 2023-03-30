package com.wakaztahir.kte.parser

import com.wakaztahir.kte.model.*
import com.wakaztahir.kte.model.model.KTEValue
import com.wakaztahir.kte.model.model.MutableKTEObject
import com.wakaztahir.kte.parser.stream.*
import com.wakaztahir.kte.parser.stream.increment
import com.wakaztahir.kte.parser.stream.parseTextWhile

private fun Char.isPlaceholderName() = this.isLetterOrDigit() || this == '_'

private fun Char.isPlaceholderDefName() = this.isPlaceholderName()

private fun SourceStream.parsePlaceHolderName(): String? {
    if (increment('(')) {
        return parseTextWhile { currentChar.isPlaceholderName() }
    }
    return null
}

private fun SourceStream.parsePlaceHolderNameAndDefinition(): Pair<String, String>? {
    val placeholderName = parsePlaceHolderName()
    if (placeholderName != null) {
        return if (increment(',')) {
            val definitionName = parseTextWhile { currentChar.isPlaceholderDefName() }
            if (increment(')')) {
                Pair(placeholderName, definitionName)
            } else {
                throw IllegalStateException("expected ')' found $currentChar when defining placeholder $placeholderName,$definitionName")
            }
        } else {
            if (increment(')')) {
                Pair(placeholderName, placeholderName)
            } else {
                throw IllegalStateException("expected ')' found $currentChar when defining placeholder $placeholderName")
            }
        }
    }
    return null
}

private fun LazyBlock.parsePlaceholderBlock(nameAndDef: Pair<String, String>): PlaceholderBlock {

    val blockValue = parseBlockSlice(
        startsWith = "@define_placeholder",
        endsWith = "@end_define_placeholder",
        allowTextOut = isWriteUnprocessedTextEnabled,
        inheritModel = false
    )

    return PlaceholderBlock(
        parentBlock = this,
        placeholderName = nameAndDef.first,
        definitionName = nameAndDef.second,
        startPointer = blockValue.startPointer,
        length = blockValue.length,
        parent = blockValue.model,
        blockEndPointer = blockValue.blockEndPointer,
        allowTextOut = isWriteUnprocessedTextEnabled,
        indentationLevel = blockValue.indentationLevel
    )

}


fun LazyBlock.parsePlaceholderDefinition(): PlaceholderDefinition? {
    if (source.currentChar == '@' && source.increment("@define_placeholder")) {
        val nameAndDef = source.parsePlaceHolderNameAndDefinition()
        if (nameAndDef != null) {
            val blockValue = parsePlaceholderBlock(nameAndDef = nameAndDef)
            return PlaceholderDefinition(
                blockValue = blockValue
            )
        } else {
            throw IllegalStateException("placeholder name is required when defining a placeholder using @define_placeholder")
        }
    }
    return null
}

fun LazyBlock.parsePlaceholderInvocation(): PlaceholderInvocation? {
    if (source.currentChar == '@' && source.increment("@placeholder")) {
        val placeholderName = source.parsePlaceHolderName()
        val genValue: KTEValue? = if (source.increment(',')) {
            val refValue = source.parseVariableReference()
            if (refValue != null) {
                if (source.increment(')')) {
                    refValue
                } else {
                    throw IllegalStateException("expected ')' found ${source.currentChar} when invoking placeholder $placeholderName")
                }
            } else {
                null
            }
        } else {
            if (source.increment(')')) {
                null
            } else {
                throw IllegalStateException("expected ')' found ${source.currentChar} when invoking placeholder $placeholderName")
            }
        }
        if (placeholderName != null) {
            return PlaceholderInvocation(
                placeholderName = placeholderName,
                invocationEndPointer = source.pointer,
                paramValue = genValue
            )
        } else {
            throw IllegalStateException("placeholder name is required when invoking a placeholder using @placeholder")
        }
    }
    return null
}

fun LazyBlock.parsePlaceholderUse(): PlaceholderUse? {
    if (source.currentChar == '@' && source.increment("@use_placeholder")) {
        val name = source.parsePlaceHolderNameAndDefinition()
        if (name != null) {
            return PlaceholderUse(placeholderName = name.first, definitionName = name.second)
        } else {
            throw IllegalStateException("placeholder name is required when invoking a placeholder using @placeholder")
        }
    }
    return null
}