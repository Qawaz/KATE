package com.wakaztahir.kte.parser.stream

import com.wakaztahir.kte.model.LazyBlock
import com.wakaztahir.kte.model.PlaceholderBlock

abstract class SourceStream : LazyBlock, PlaceholderManager, EmbeddingManager {

    override val source: SourceStream
        get() = this

    override val allowTextOut: Boolean
        get() = true

    abstract val pointer: Int

    abstract val currentChar: Char

    abstract val hasEnded: Boolean

    abstract fun incrementPointer(): Boolean

    abstract fun decrementPointer(decrease: Int = 1): Boolean

    abstract fun setPointerAt(position: Int): Boolean

    override fun canIterate(): Boolean = !hasEnded


    override val placeholders = mutableListOf<PlaceholderBlock>()
    override val undefinedPlaceholders = mutableListOf<PlaceholderBlock>()
    override val embeddedStreams = mutableMapOf<String, Boolean>()

}