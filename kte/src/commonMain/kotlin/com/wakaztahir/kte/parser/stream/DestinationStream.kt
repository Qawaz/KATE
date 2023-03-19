package com.wakaztahir.kte.parser.stream

abstract class DestinationStream {
    abstract fun write(char : Char)
    abstract fun write(str : String)
}