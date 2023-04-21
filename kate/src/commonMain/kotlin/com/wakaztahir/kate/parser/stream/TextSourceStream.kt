package com.wakaztahir.kate.parser.stream

import com.wakaztahir.kate.GlobalModelObjectName
import com.wakaztahir.kate.dsl.ModelObjectImpl
import com.wakaztahir.kate.model.KATEType
import com.wakaztahir.kate.model.model.MutableKATEObject
import com.wakaztahir.kate.runtime.GlobalObjectImplementation

class TextSourceStream(
    private val sourceCode: String,
    override val model: MutableKATEObject = ModelObjectImpl(objectName = GlobalModelObjectName,itemType = KATEType.Any),
    override val placeholderManager: PlaceholderManager = EmptyPlaceholderManager(),
    override val embeddingManager: EmbeddingManager = NoEmbeddings
) : SourceStream() {

    init {
        DefaultPlaceholderManagerInitializer.initializerDefaultPlaceholders(this)
        GlobalObjectImplementation.putIntoObject(model)
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