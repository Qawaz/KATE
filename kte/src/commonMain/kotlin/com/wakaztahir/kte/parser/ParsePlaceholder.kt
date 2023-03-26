package com.wakaztahir.kte.parser

import com.wakaztahir.kte.dsl.ScopedModelObject
import com.wakaztahir.kte.model.*
import com.wakaztahir.kte.model.model.MutableKTEObject
import com.wakaztahir.kte.parser.stream.*
import com.wakaztahir.kte.parser.stream.escapeBlockSpacesForward
import com.wakaztahir.kte.parser.stream.increment
import com.wakaztahir.kte.parser.stream.parseTextWhile

private fun Char.isPlaceholderName() = this.isLetterOrDigit() || this == '_'

private fun Char.isPlaceholderDefName() = this.isPlaceholderName()

private fun SourceStream.parsePlaceholderNameOnly(): String? {
    if (increment('(')) {
        val placeholderName = parseTextWhile { currentChar.isPlaceholderName() }
        if (increment(')')) {
            return placeholderName
        } else {
            throw IllegalStateException("expected ')' found $currentChar when defining placeholder $placeholderName")
        }
    }
    return null
}

private fun SourceStream.parsePlaceHolderNameAndDefinition(): Pair<String, String>? {
    if (increment('(')) {
        val placeholderName = parseTextWhile { currentChar.isPlaceholderName() }
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
        allowTextOut = allowTextOut,
        inheritModel = true
    )

    return PlaceholderBlock(
        source = source,
        placeholderName = nameAndDef.first,
        definitionName = nameAndDef.second,
        startPointer = blockValue.startPointer,
        length = blockValue.length,
        parent = blockValue.model,
        blockEndPointer = blockValue.blockEndPointer,
        allowTextOut = allowTextOut
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
        val placeholderName = source.parsePlaceholderNameOnly()
        if (placeholderName != null) {
            return PlaceholderInvocation(placeholderName = placeholderName, invocationEndPointer = source.pointer)
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