package com.wakaztahir.kate.parser

import com.wakaztahir.kate.dsl.ScopedModelObject
import com.wakaztahir.kate.model.*
import com.wakaztahir.kate.model.model.MutableKTEObject
import com.wakaztahir.kate.parser.stream.*
import com.wakaztahir.kate.parser.stream.increment

fun LazyBlock.parseBlockSlice(
    startsWith: String,
    endsWith: String,
    allowTextOut: Boolean,
    model: MutableKTEObject,
    indentationLevel: Int = this.indentationLevel + 1
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
        model = model,
        blockEndPointer = source.pointer,
        isWriteUnprocessedTextEnabled = allowTextOut,
        indentationLevel = indentationLevel
    )

}

fun LazyBlock.parseBlockSlice(
    startsWith: String,
    endsWith: String,
    allowTextOut: Boolean,
    inheritModel: Boolean,
    indentationLevel: Int = this.indentationLevel + 1
): LazyBlockSlice = parseBlockSlice(
    startsWith = startsWith,
    endsWith = endsWith,
    allowTextOut = allowTextOut,
    model = if (inheritModel) model else ScopedModelObject(model),
    indentationLevel = indentationLevel
)

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