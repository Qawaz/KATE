package com.wakaztahir.kte.parser

import com.wakaztahir.kte.dsl.ScopedModelObject
import com.wakaztahir.kte.model.*
import com.wakaztahir.kte.parser.stream.*
import com.wakaztahir.kte.parser.stream.increment

fun LazyBlock.parseBlockSlice(
    startsWith: String,
    endsWith: String,
    allowTextOut: Boolean,
    inheritModel: Boolean
): LazyBlockSlice {

    escapeBlockSpacesForward()

    val previous = source.pointer

    val ender: String = source.incrementUntilDirectiveWithSkip(startsWith) {
        if (source.increment(endsWith)) endsWith else null
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
        model = if (inheritModel) model else ScopedModelObject(model),
        blockEndPointer = source.pointer,
        isWriteUnprocessedTextEnabled = allowTextOut,
        indentationLevel = indentationLevel + 1
    )

}

fun LazyBlock.parseRawBlock(): RawBlock? {
    if (source.currentChar == '@' && source.increment("@raw")) {
        return RawBlock(
            parseBlockSlice(
                startsWith = "@raw",
                endsWith = "@endraw",
                allowTextOut = false,
                inheritModel = true
            )
        )
    }
    return null
}

fun LazyBlock.parsePartialRaw(): PartialRawBlock? {
    if (source.currentChar == '@' && source.increment("@partial_raw")) {
        val slice = parseBlockSlice(
            startsWith = "@partial_raw",
            endsWith = "@end_partial_raw",
            allowTextOut = false,
            inheritModel = true
        )
        return PartialRawBlock(
            value = PartialRawLazyBlockSlice(
                parentBlock = this,
                startPointer = slice.startPointer,
                length = slice.length,
                blockEndPointer = slice.blockEndPointer,
                model = slice.model,
                indentationLevel = slice.indentationLevel
            )
        )
    }
    return null
}

fun LazyBlock.parseDefaultNoRaw(): DefaultNoRawBlock? {
    if (source.currentChar == '@' && source.increment("@default_no_raw")) {
        return DefaultNoRawBlock(
            parseBlockSlice(
                startsWith = "@default_no_raw",
                endsWith = "@end_default_no_raw",
                allowTextOut = true,
                inheritModel = true
            )
        )
    }
    return null
}