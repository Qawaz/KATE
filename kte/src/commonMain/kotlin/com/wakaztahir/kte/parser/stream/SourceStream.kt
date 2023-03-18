package com.wakaztahir.kte.parser.stream

abstract class SourceStream {

    abstract val pointer: Int

    abstract val currentChar: Char

    abstract val hasEnded : Boolean

    abstract fun incrementPointer(): Boolean

    abstract fun decrementPointer(decrease: Int): Boolean

}