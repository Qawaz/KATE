package com.wakaztahir.kte

import com.wakaztahir.kte.parser.stream.DestinationStream
import com.wakaztahir.kte.parser.stream.WritableStream
import java.io.OutputStream

class OutputStreamDestination(private val outputStream: OutputStream) : WritableStream, DestinationStream {

    override val stream: WritableStream
        get() = this

    override fun write(char: Char) {
        outputStream.write(char.code)
    }

    override fun write(str: String) {
        for (char in str) {
            write(char)
        }
    }
}