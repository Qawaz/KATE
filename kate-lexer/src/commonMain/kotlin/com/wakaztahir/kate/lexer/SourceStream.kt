package com.wakaztahir.kate.lexer

interface SourceStream {

    val pointer: Int

    val currentChar: Char

    val hasEnded: Boolean

    // Return the character at pointer + offset without incrementing pointer
    fun lookAhead(offset: Int): Char?

    // Peak ahead at the current pointer by length and return the text without incrementing pointer
    fun lookAhead(offset : Int, length: Int): String?

    // Increments the stream pointer by 1
    fun incrementPointer() : Boolean

    // Increments the stream pointer by amount
    fun increment(amount: Int) : Boolean

}