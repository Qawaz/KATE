package com.wakaztahir.kte.parser.stream

import com.wakaztahir.kte.model.LazyBlock

abstract class SourceStream : LazyBlock {

    abstract val pointer: Int

    abstract val currentChar: Char

    abstract val hasEnded: Boolean

    abstract fun incrementPointer(): Boolean

    abstract fun decrementPointer(decrease: Int = 1): Boolean

    abstract fun setPointerAt(position: Int): Boolean

    override fun canIterate(stream: SourceStream): Boolean {
        return !hasEnded
    }

}