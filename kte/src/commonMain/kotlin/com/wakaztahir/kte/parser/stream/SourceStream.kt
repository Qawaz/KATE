package com.wakaztahir.kte.parser.stream

import com.wakaztahir.kte.model.BlockParser

abstract class SourceStream : BlockParser {

    abstract val pointer: Int

    abstract val currentChar: Char

    abstract val hasEnded: Boolean

    abstract fun incrementPointer(increase: Int = 1): Boolean

    abstract fun decrementPointer(decrease: Int = 1): Boolean

    abstract fun setPointerAt(position: Int): Boolean

    override fun hasNext(stream: SourceStream): Boolean = !hasEnded

}