package com.wakaztahir.kate.parser.stream

import com.wakaztahir.kate.GlobalModelObjectName
import com.wakaztahir.kate.dsl.ModelObjectImpl
import com.wakaztahir.kate.model.model.MutableKTEObject

class TextSourceStream(
    private val sourceCode: String,
    override val model: MutableKTEObject = ModelObjectImpl(GlobalModelObjectName),
    override val placeholderManager: PlaceholderManager = EmptyPlaceholderManager(),
    override val embeddingManager: EmbeddingManager = NoEmbeddings
) : SourceStream() {

    init {
        DefaultPlaceholderManagerInitializer.initializerDefaultPlaceholders(this)
    }

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

}