package com.wakaztahir.kte

import com.wakaztahir.kte.dsl.ModelObjectImpl
import com.wakaztahir.kte.model.model.MutableKTEObject
import com.wakaztahir.kte.parser.stream.EmbeddingManager
import com.wakaztahir.kte.parser.stream.PlaceholderManager
import com.wakaztahir.kte.parser.stream.SourceStream
import java.io.InputStream

class InputSourceStream(
    val inputStream: InputStream,
    override val model: MutableKTEObject = ModelObjectImpl("Global"),
    override val embeddingManager: EmbeddingManager = NoEmbeddings,
    override val placeholderManager: PlaceholderManager = EmptyPlaceholderManager()
) : SourceStream() {

    override var pointer: Int = 0

    private var currentCode: Int = inputStream.read()

    override val currentChar: Char
        get() = currentCode.toChar()

    override val hasEnded: Boolean
        get() = currentCode == -1

    override fun incrementPointer(): Boolean {
        val nextCode = inputStream.read()
        return if (nextCode == -1) {
            false
        } else {
            pointer++
            currentCode = nextCode
            true
        }
    }

    override fun decrementPointer(decrease: Int): Boolean {
        if (decrease == 0 || pointer - decrease < 0) {
            return false
        }
        inputStream.reset()
        inputStream.mark(pointer - decrease)
        for (i in 1..decrease) {
            currentCode = inputStream.read()
        }
        pointer -= decrease
        return true
    }

    override fun setPointerAt(position: Int): Boolean {
        if (position < 0) {
            return false
        }
        inputStream.reset()
        inputStream.mark(position)
        for (i in 1..position) {
            currentCode = inputStream.read()
        }
        pointer = position
        return true
    }
}