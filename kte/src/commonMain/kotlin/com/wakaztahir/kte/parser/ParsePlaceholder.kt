package com.wakaztahir.kte.parser

import com.wakaztahir.kte.model.*
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

private fun SourceStream.parsePlaceholderBlock(nameAndDef: Pair<String, String>): PlaceholderBlock {
    val source = this
    source.escapeBlockSpacesForward()

    val previous = source.pointer

    val ender: String = source.incrementUntilDirectiveWithSkip("@define_placeholder") {
        if (source.increment("@end_define_placeholder")) "@end_define_placeholder" else null
    } ?: throw IllegalStateException("@define_placeholder must end with @end_define_placeholder")

    source.decrementPointer(ender.length)

    val pointerBeforeEnder = source.pointer

    source.escapeBlockSpacesBackward()

    val length = source.pointer - previous

    source.setPointerAt(pointerBeforeEnder + ender.length)

    return PlaceholderBlock(
        placeholderName = nameAndDef.first,
        definitionName = nameAndDef.second,
        startPointer = previous,
        length = length,
        parent = this@parsePlaceholderBlock.model,
        blockEndPointer = source.pointer
    )

}


fun SourceStream.parsePlaceholderDefinition(): PlaceholderDefinition? {
    if (currentChar == '@' && increment("@define_placeholder")) {
        val nameAndDef = parsePlaceHolderNameAndDefinition()
        if (nameAndDef != null) {
            val blockValue = parsePlaceholderBlock(nameAndDef = nameAndDef)
            return PlaceholderDefinition(
                placeholder = blockValue
            )
        } else {
            throw IllegalStateException("placeholder name is required when defining a placeholder using @define_placeholder")
        }
    }
    return null
}

fun SourceStream.parsePlaceholderInvocation(): PlaceholderInvocation? {
    if (currentChar == '@' && increment("@placeholder")) {
        val placeholderName = parsePlaceholderNameOnly()
        if (placeholderName != null) {
            return PlaceholderInvocation(placeholderName = placeholderName, invocationEndPointer = pointer)
        } else {
            throw IllegalStateException("placeholder name is required when invoking a placeholder using @placeholder")
        }
    }
    return null
}

fun SourceStream.parsePlaceholderUse(): PlaceholderUse? {
    if (currentChar == '@' && increment("@use_placeholder")) {
        val name = parsePlaceHolderNameAndDefinition()
        if (name != null) {
            return PlaceholderUse(placeholderName = name.first, definitionName = name.second)
        } else {
            throw IllegalStateException("placeholder name is required when invoking a placeholder using @placeholder")
        }
    }
    return null
}