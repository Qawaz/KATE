package com.wakaztahir.kte.parser

import com.wakaztahir.kte.dsl.ScopedModelObject
import com.wakaztahir.kte.model.LazyBlock
import com.wakaztahir.kte.model.LazyBlockSlice
import com.wakaztahir.kte.model.RawBlock
import com.wakaztahir.kte.parser.stream.*
import com.wakaztahir.kte.parser.stream.increment

fun LazyBlock.parseBlockSlice(
    startsWith: String,
    endsWith: String,
    allowTextOut: Boolean,
    inheritModel: Boolean
): LazyBlockSlice {

    source.escapeBlockSpacesForward()

    val previous = source.pointer

    val ender: String = source.incrementUntilDirectiveWithSkip(startsWith) {
        if (source.increment(endsWith)) endsWith else null
    } ?: throw IllegalStateException("$startsWith must end with $endsWith")


    source.decrementPointer(ender.length)

    val pointerBeforeEnder = source.pointer

    source.escapeBlockSpacesBackward()

    val length = source.pointer - previous

    source.setPointerAt(pointerBeforeEnder + ender.length)

    return LazyBlockSlice(
        source = source,
        startPointer = previous,
        length = length,
        model = if (inheritModel) model else ScopedModelObject(model),
        blockEndPointer = source.pointer,
        allowTextOut = allowTextOut
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