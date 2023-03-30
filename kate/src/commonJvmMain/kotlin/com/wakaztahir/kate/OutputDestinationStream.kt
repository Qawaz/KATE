package com.wakaztahir.kate

import com.wakaztahir.kate.parser.stream.DestinationStream
import com.wakaztahir.kate.parser.stream.WritableStream
import java.io.OutputStream

class OutputDestinationStream(val outputStream: OutputStream) : WritableStream, DestinationStream {

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