package com.wakaztahir.kte.parser.stream

class TextDestinationStream : WritableStream, DestinationStream {

    override val stream: WritableStream
        get() = this

    private var text = ""

    override fun write(char: Char) {
        text += char
    }

    override fun write(str: String) {
        text += str
    }

    fun getValue(): String {
        return text
    }

}