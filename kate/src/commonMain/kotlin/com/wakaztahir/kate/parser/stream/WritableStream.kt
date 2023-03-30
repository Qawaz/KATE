package com.wakaztahir.kate.parser.stream

interface WritableStream {
    fun write(char: Char)
    fun write(str: String)
}