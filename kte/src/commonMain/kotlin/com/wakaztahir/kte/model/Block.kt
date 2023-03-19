package com.wakaztahir.kte.model

import com.wakaztahir.kte.parser.stream.SourceStream

class Block {

}

class LazyBlockSlice(
    val pointer: Int,
    val length: Int
) {
    fun getValueAsString(stream: SourceStream): String {
        val previous = stream.pointer
        stream.setPointerAt(pointer)
        var text = ""
        while (stream.pointer < pointer + length) {
            text += stream.currentChar
            stream.incrementPointer()
        }
        stream.setPointerAt(previous)
        return text
    }
}