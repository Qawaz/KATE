package com.wakaztahir.kte.parser

import com.wakaztahir.kte.model.LazyBlock
import com.wakaztahir.kte.model.LazyBlockSlice
import com.wakaztahir.kte.model.RawBlock
import com.wakaztahir.kte.parser.stream.*
import com.wakaztahir.kte.parser.stream.increment

fun LazyBlock.parseRawBlock(): RawBlock? {
    return if (source.currentChar == '@' && source.increment("@raw")) {

        source.escapeBlockSpacesForward()

        val previous = source.pointer

        val ender: String = source.incrementUntilDirectiveWithSkip("@raw") {
            if (source.increment("@endraw")) "@endraw" else null
        } ?: throw IllegalStateException("@raw must end with @endraw")


        source.decrementPointer(ender.length)

        val pointerBeforeEnder = source.pointer

        source.escapeBlockSpacesBackward()

        val length = source.pointer - previous

        source.setPointerAt(pointerBeforeEnder + ender.length)

        return RawBlock(
            value = LazyBlockSlice(
                source = source,
                startPointer = previous,
                length = length,
                model = model,
                blockEndPointer = source.pointer
            )
        )

    } else {
        null
    }
}