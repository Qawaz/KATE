package com.wakaztahir.kte.parser.stream

import com.wakaztahir.kte.dsl.ModelObjectImpl
import com.wakaztahir.kte.model.LazyBlock
import com.wakaztahir.kte.model.model.MutableKTEObject

class TextSourceStream(
    private val sourceCode: String,
    override val model: MutableKTEObject = ModelObjectImpl("Global")
) : SourceStream() {

    override var pointer: Int = 0

    override val currentChar: Char
        get() = sourceCode[pointer]

    override val hasEnded get() = sourceCode.length == pointer

    override fun incrementPointer(): Boolean {
        return setPointerAt(pointer + 1)
    }

    override fun decrementPointer(decrease: Int): Boolean {
        if (decrease == 0) return true
        return setPointerAt(pointer - decrease)
    }

    override fun setPointerAt(position: Int): Boolean {
        return if (position <= sourceCode.length && position >= 0) {
            pointer = position
            true
        } else {
            false
        }
    }

    override fun provideStream(block: LazyBlock, path: String): SourceStream? {
        TODO("Not yet implemented")
    }

}