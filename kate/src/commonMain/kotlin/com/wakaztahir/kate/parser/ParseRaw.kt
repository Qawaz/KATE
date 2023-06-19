package com.wakaztahir.kate.parser

import com.wakaztahir.kate.dsl.ScopedModelLazyParent
import com.wakaztahir.kate.lexer.tokens.StaticTokens
import com.wakaztahir.kate.lexer.tokens.StringStaticToken
import com.wakaztahir.kate.model.*
import com.wakaztahir.kate.model.model.MutableKATEObject
import com.wakaztahir.kate.parser.block.ParsedBlock
import com.wakaztahir.kate.parser.stream.*
import com.wakaztahir.kate.parser.stream.increment

fun LazyBlock.parseBlockSlice(
    startsWith: StringStaticToken,
    endsWith: StringStaticToken,
    isDefaultNoRaw: Boolean,
    provider: ModelProvider,
    indentationLevel: Int = this.indentationLevel + 1
): LazyBlockSlice {

    escapeBlockSpacesForward()

    val previous = source.pointer

    val ender: String = source.incrementUntilDirectiveWithSkip(startsWith) {
        source.incrementAndReturnDirective(endsWith)
    } ?: throw IllegalStateException("$startsWith must end with $endsWith")


    source.decrementPointer(ender.length)

    val pointerBeforeEnder = source.pointer

    escapeBlockSpacesBackward()

    val length = source.pointer - previous

    source.setPointerAt(pointerBeforeEnder + ender.length)

    return LazyBlockSlice(
        parentBlock = this,
        startPointer = previous,
        length = length,
        provider = provider,
        blockEndPointer = source.pointer,
        isDefaultNoRaw = isDefaultNoRaw,
        indentationLevel = indentationLevel
    )

}

fun LazyBlock.parseBlockSlice(
    startsWith: StringStaticToken,
    endsWith: StringStaticToken,
    isDefaultNoRaw: Boolean,
    inheritModel: Boolean,
    indentationLevel: Int = this.indentationLevel + 1
): LazyBlockSlice = parseBlockSlice(
    startsWith = startsWith,
    endsWith = endsWith,
    isDefaultNoRaw = isDefaultNoRaw,
    provider = if (inheritModel) provider else ModelProvider.Single(ScopedModelLazyParent { model }),
    indentationLevel = indentationLevel
)

private fun String.containsAt(index: Int, str: String): Boolean {
    if (index + str.length > length) return false
    var i = index
    for (char in str) {
        if (this[i] != char) return false
        i++
    }
    return true
}

private fun String.deIndented(indentationLevel: Int): String {
    var i = 0
    var text = ""
    while (i < length) {
        if ((i == 0 || this[i - 1] == '\n') &&
            (this[i] == '\t' || (this.containsAt(i, "    ")))
            && indentationLevel > 0
        ) {
            var x = 0
            while (x < indentationLevel && i < length) {
                if (this[i] == '\t') {
                    i++
                } else if (containsAt(index = i, str = "    ")) {
                    i += 4
                }
                x++
            }
            continue
        } else {
            text += this[i]
        }
        i++
    }
    return text
}

fun LazyBlock.parseRawBlockText(): String {
    escapeBlockSpacesForward()
    var text = source.parseTextUntilConsumedDirectiveNew(StaticTokens.EndRaw)
        ?: throw IllegalStateException("@raw must end with @endraw")
    text = text.deIndented(indentationLevel + 1)
    return text.escapeBlockSpacesBackward(indentationLevel)
}

fun LazyBlock.parseRawBlock(): RawBlock? {
    if (source.incrementDirective(StaticTokens.Raw)) {
        return RawBlock(parseRawBlockText())
    }
    return null
}

class PartialRawParsedBlock(val model: MutableKATEObject, codeGens: List<CodeGenRange>) : ParsedBlock(codeGens) {
    override fun generateTo(destination: DestinationStream) {
        for (range in codeGens) {
            range.gen.generateTo(destination = destination)
        }
    }
}

fun LazyBlock.parsePartialRaw(): PartialRawBlock? {
    if (source.incrementDirective(StaticTokens.PartialRaw)) {

        val slice = parseBlockSlice(
            startsWith = StaticTokens.PartialRaw,
            endsWith = StaticTokens.EndPartialRaw,
            isDefaultNoRaw = false,
            inheritModel = true
        )

        val partialRawSlice = PartialRawLazyBlockSlice(
            parentBlock = this,
            startPointer = slice.startPointer,
            length = slice.length,
            blockEndPointer = slice.blockEndPointer,
            provider = slice.provider,
            indentationLevel = slice.indentationLevel
        )

        return PartialRawBlock(
            parsedBlock = PartialRawParsedBlock(
                model = partialRawSlice.model,
                codeGens = partialRawSlice.parse().codeGens,
            )
        )
    }
    return null
}

fun LazyBlock.parseDefaultNoRaw(): DefaultNoRawBlock? {
    if (source.incrementDirective(StaticTokens.DefaultNoRaw)) {
        return DefaultNoRawBlock(
            parseBlockSlice(
                startsWith = StaticTokens.DefaultNoRaw,
                endsWith = StaticTokens.EndDefaultNoRaw,
                isDefaultNoRaw = true,
                inheritModel = true
            ).parse()
        )
    }
    return null
}