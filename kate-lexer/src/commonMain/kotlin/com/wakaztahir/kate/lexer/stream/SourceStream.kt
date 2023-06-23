package com.wakaztahir.kate.lexer.stream

interface SourceStream : SequentialStream {

    fun <T> lookAhead(block: LookAheadScope.() -> T): T

    // Return the character at pointer + offset without incrementing pointer
    fun lookAhead(offset: Int): Char?

    // Peak ahead at the current pointer by length and return the text without incrementing pointer
    fun lookAhead(offset: Int, length: Int): String?

}