package com.wakaztahir.kte

import com.wakaztahir.kte.dsl.ModelObjectImpl
import com.wakaztahir.kte.model.model.MutableKTEObject
import com.wakaztahir.kte.parser.stream.DefaultPlaceholderManagerInitializer
import com.wakaztahir.kte.parser.stream.EmbeddingManager
import com.wakaztahir.kte.parser.stream.PlaceholderManager
import com.wakaztahir.kte.parser.stream.SourceStream
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.charset.Charset

class InputSourceStream(
    private val inputStream: InputStream,
    override val model: MutableKTEObject = ModelObjectImpl("Global"),
    override val embeddingManager: EmbeddingManager = NoEmbeddings,
    override val placeholderManager: PlaceholderManager = EmptyPlaceholderManager()
) : SourceStream() {

    init {
        DefaultPlaceholderManagerInitializer.initializerDefaultPlaceholders(this)
    }

    override var pointer: Int = 0

    private val currentInt : Int
        get(){
            inputStream.mark(pointer + 1)
            inputStream.skip(pointer.toLong())
            val character = inputStream.read()
            inputStream.reset()
            return character
        }

    override val currentChar: Char
        get() {
            return currentInt.toChar()
        }


    override val hasEnded: Boolean
        get() = currentInt == -1

    override fun incrementPointer(): Boolean {
        return setPointerAt(pointer + 1)
    }

    override fun decrementPointer(decrease: Int): Boolean {
        if (decrease == 0) return true
        return setPointerAt(pointer - decrease)
    }

    override fun setPointerAt(position: Int): Boolean {
        if (position >= 0) {
            pointer = position
            return true
        }
        return false
    }

}