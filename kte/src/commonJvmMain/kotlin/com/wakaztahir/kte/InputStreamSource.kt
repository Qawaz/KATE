package com.wakaztahir.kte

import com.wakaztahir.kte.model.model.MutableTemplateModel
import com.wakaztahir.kte.parser.stream.SourceStream
import java.io.InputStream

class InputStreamSource(val stream: InputStream) : SourceStream() {

    override var currentChar: Char = stream.read().toChar()
        private set

    override var hasEnded = currentChar == (-1).toChar()
        private set

    override var pointer: Int = 0
        private set

    override fun incrementPointer(): Boolean {
        if (hasEnded) {
            return false
        }
        val nextChar = stream.read().toChar()
        if (nextChar == (-1).toChar()) {
            hasEnded = true
            return false
        }
        currentChar = nextChar
        pointer++
        return true
    }

    override fun decrementPointer(decrease: Int): Boolean {
        if (decrease == 0 || pointer == 0) {
            return true
        }
        val newPosition = pointer - decrease
        if (newPosition < 0) {
            return false
        }
        stream.reset()
        stream.skip(newPosition.toLong())
        currentChar = stream.read().toChar()
        pointer = newPosition
        return true
    }

    override fun setPointerAt(position: Int): Boolean {
        TODO("")
    }

    override val model: MutableTemplateModel
        get() = TODO("Not yet implemented")

}