package com.wakaztahir.kate.parser

import com.wakaztahir.kate.model.*
import com.wakaztahir.kate.parser.stream.*
import com.wakaztahir.kate.parser.stream.increment
import com.wakaztahir.kate.parser.stream.parseTextWhile
import com.wakaztahir.kate.parser.variable.isVariableName

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

private fun <T> SourceStream.parsePlaceHolderNameAndDefinitionAndParameter(parseParameter: SourceStream.() -> T?): Triple<String, String?, T?>? {
    val placeholderName = parsePlaceHolderName()
    if (placeholderName != null) {
        return if (increment(',')) {
            val definitionName = parseTextWhile { currentChar.isPlaceholderDefName() }.ifEmpty { null }
            if (increment(')')) {
                Triple(placeholderName, definitionName, null)
            } else {
                if (increment(',')) {
                    val parameterName = parseParameter()
                    if (increment(')')) {
                        Triple(placeholderName, definitionName, parameterName)
                    } else {
                        throw IllegalStateException("expected ')' found $currentChar when defining placeholder $placeholderName,$definitionName,$parameterName")
                    }
                } else {
                    throw IllegalStateException("expected ')' or ',' found $currentChar when defining placeholder $placeholderName,$definitionName")
                }
            }
        } else {
            if (increment(')')) {
                Triple(placeholderName, null, null)
            } else {
                throw IllegalStateException("expected ')' found $currentChar when defining placeholder $placeholderName")
            }
        }
    }
    return null
}

private fun LazyBlock.parsePlaceholderBlock(nameAndDef: Triple<String, String?, String?>): PlaceholderBlock {

    val blockValue = parseBlockSlice(
        startsWith = "@define_placeholder",
        endsWith = "@end_define_placeholder",
        isDefaultNoRaw = isDefaultNoRaw,
        inheritModel = false
    )

    return PlaceholderBlock(
        parentBlock = this,
        placeholderName = nameAndDef.first,
        definitionName = nameAndDef.second ?: nameAndDef.first,
        parameterName = nameAndDef.third,
        startPointer = blockValue.startPointer,
        length = blockValue.length,
        model = blockValue.model,
        blockEndPointer = blockValue.blockEndPointer,
        allowTextOut = isDefaultNoRaw,
        indentationLevel = blockValue.indentationLevel
    )

}


fun LazyBlock.parsePlaceholderDefinition(): PlaceholderDefinition? {
    if (source.currentChar == '@' && source.increment("@define_placeholder")) {
        val isOnce = source.increment("_once")
        val nameAndDef = source.parsePlaceHolderNameAndDefinitionAndParameter(
            parseParameter = {
                parseTextWhile { currentChar.isVariableName() }.ifEmpty { null }
            }
        )
        if (nameAndDef != null) {
            val blockValue = parsePlaceholderBlock(nameAndDef = nameAndDef)
            return PlaceholderDefinition(
                blockValue = blockValue,
                isOnce = isOnce
            )
        } else {
            throw IllegalStateException("placeholder name is required when defining a placeholder using @define_placeholder")
        }
    }
    return null
}

fun LazyBlock.parsePlaceholderInvocation(): PlaceholderInvocation? {
    if (source.currentChar == '@' && source.increment("@placeholder")) {
        val triple = source.parsePlaceHolderNameAndDefinitionAndParameter {
            parseAnyExpressionOrValue(
                parseDirectRefs = true
            )
        }
        if (triple != null) {
            return PlaceholderInvocation(
                placeholderName = triple.first,
                definitionName = triple.second,
                invocationEndPointer = source.pointer,
                paramValue = triple.third
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