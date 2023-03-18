package com.wakaztahir.kte.parser.stream

abstract class SourceStream {


    // Size of the stream , in case of text , its length
    abstract val streamSize: Int

    // Currently pointing at position in the stream
    abstract val pointer: Int

    abstract val currentChar: Char

    val hasEnded get() = streamSize == pointer

    abstract fun incrementPointer(): Boolean

    abstract fun decrementPointer(decrease: Int): Boolean

}