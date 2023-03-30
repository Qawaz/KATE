package com.wakaztahir.kte.parser.stream

interface WritableStream {
    fun write(char: Char)
    fun write(str: String)
}