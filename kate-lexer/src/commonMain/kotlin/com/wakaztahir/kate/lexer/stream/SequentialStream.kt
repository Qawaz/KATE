package com.wakaztahir.kate.lexer.stream

interface SequentialStream {

    val hasEnded: Boolean

    val pointer: Int

    val currentChar: Char

    val lineNumber : Int

    val columnNumber : Int

    // Increments the stream pointer by 1
    fun incrementPointer(): Boolean

    // Increments the stream pointer by amount
    fun incrementPointer(amount: Int): Boolean

}