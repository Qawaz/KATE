package com.wakaztahir.kate.parser

import com.wakaztahir.kate.lexer.tokens.StaticTokens
import com.wakaztahir.kate.model.*
import com.wakaztahir.kate.parser.stream.ParserSourceStream
import com.wakaztahir.kate.parser.stream.increment
import com.wakaztahir.kate.parser.stream.incrementDirective
import com.wakaztahir.kate.parser.stream.parseTextWhile
import com.wakaztahir.kate.parser.variable.isVariableName

private fun Char.isPlaceholderName() = this.isLetterOrDigit() || this == '_'

private fun Char.isPlaceholderDefName() = this.isPlaceholderName()

private fun ParserSourceStream.parsePlaceHolderName(): String? {
    if (increment(StaticTokens.LeftParenthesis)) {
        return parseTextWhile { currentChar.isPlaceholderName() }
    }
    return null
}

private fun ParserSourceStream.parsePlaceHolderNameAndDefinition(): Pair<String, String>? {
    val placeholderName = parsePlaceHolderName()
    if (placeholderName != null) {
        return if (increment(StaticTokens.Comma)) {
            val definitionName = parseTextWhile { currentChar.isPlaceholderDefName() }
            if (increment(StaticTokens.RightParenthesis)) {
                Pair(placeholderName, definitionName)
            } else {
                throw IllegalStateException("expected '${StaticTokens.RightParenthesis}' found $currentChar when defining placeholder $placeholderName,$definitionName")
            }
        } else {
            if (increment(StaticTokens.RightParenthesis)) {
                Pair(placeholderName, placeholderName)
            } else {
                throw IllegalStateException("expected '${StaticTokens.RightParenthesis}' found $currentChar when defining placeholder $placeholderName")
            }
        }
    }
    return null
}

private fun <T> ParserSourceStream.parsePlaceHolderNameAndDefinitionAndParameter(parseParameter: ParserSourceStream.() -> T?): Triple<String, String?, T?>? {
    val placeholderName = parsePlaceHolderName()
    if (placeholderName != null) {
        return if (increment(StaticTokens.Comma)) {
            val definitionName = parseTextWhile { currentChar.isPlaceholderDefName() }.ifEmpty { null }
            if (increment(StaticTokens.RightParenthesis)) {
                Triple(placeholderName, definitionName, null)
            } else {
                if (increment(StaticTokens.Comma)) {
                    val parameterName = parseParameter()
                    if (increment(StaticTokens.RightParenthesis)) {
                        Triple(placeholderName, definitionName, parameterName)
                    } else {
                        throw IllegalStateException("expected '${StaticTokens.RightParenthesis}' found $currentChar when defining placeholder $placeholderName,$definitionName,$parameterName")
                    }
                } else {
                    throw IllegalStateException("expected '${StaticTokens.RightParenthesis}' or '${StaticTokens.Comma}' found $currentChar when defining placeholder $placeholderName,$definitionName")
                }
            }
        } else {
            if (increment(StaticTokens.RightParenthesis)) {
                Triple(placeholderName, null, null)
            } else {
                throw IllegalStateException("expected '${StaticTokens.RightParenthesis}' found $currentChar when defining placeholder $placeholderName")
            }
        }
    }
    return null
}

private fun LazyBlock.parsePlaceholderBlock(nameAndDef: Triple<String, String?, String?>): PlaceholderParsedBlock {

    val provider = ModelProvider.LateInit()

    val block = parseBlockSlice(
        startsWith = StaticTokens.DefinePlaceholder,
        endsWith = StaticTokens.EndDefinePlaceholder,
        isDefaultNoRaw = isDefaultNoRaw,
        provider = provider
    )

    val parsedBlock = block.parse()

    return PlaceholderParsedBlock(
        provider = provider,
        placeholderName = nameAndDef.first,
        definitionName = nameAndDef.second ?: nameAndDef.first,
        parameterName = nameAndDef.third ?: "__param__",
        codeGens = parsedBlock.codeGens
    )

}


fun LazyBlock.parsePlaceholderDefinition(): PlaceholderDefinition? {
    if (source.incrementDirective(StaticTokens.DefinePlaceholder)) {
        val isOnce = source.increment(StaticTokens.UnderscoreOnce)
        val nameAndDef = source.parsePlaceHolderNameAndDefinitionAndParameter(
            parseParameter = {
                parseTextWhile { currentChar.isVariableName() }.ifEmpty { null }
            }
        )
        if (nameAndDef != null) {
            val blockValue = parsePlaceholderBlock(nameAndDef = nameAndDef)
            return PlaceholderDefinition(
                parsedBlock = blockValue,
                isOnce = isOnce,
                placeholderManager = source.placeholderManager
            )
        } else {
            throw IllegalStateException("placeholder name is required when defining a placeholder using @define_placeholder")
        }
    }
    return null
}

fun LazyBlock.parsePlaceholderInvocation(): PlaceholderInvocation? {
    if (source.incrementDirective(StaticTokens.PlaceholderCall)) {
        val triple = source.parsePlaceHolderNameAndDefinitionAndParameter {
            parseAnyExpressionOrValue(
                parseDirectRefs = true
            )
        }
        if (triple != null) {
            return PlaceholderInvocation(
                placeholderName = triple.first,
                definitionName = triple.second,
                paramValue = triple.third,
                placeholderManager = source.placeholderManager,
                invocationProvider = provider
            )
        } else {
            throw IllegalStateException("placeholder name is required when invoking a placeholder using @placeholder")
        }
    }
    return null
}

fun LazyBlock.parsePlaceholderUse(): PlaceholderUse? {
    if (source.incrementDirective(StaticTokens.PlaceholderUse)) {
        val name = source.parsePlaceHolderNameAndDefinition()
        if (name != null) {
            return PlaceholderUse(
                placeholderName = name.first,
                definitionName = name.second,
                placeholderManager = source.placeholderManager
            )
        } else {
            throw IllegalStateException("placeholder name is required when invoking a placeholder using @placeholder")
        }
    }
    return null
}