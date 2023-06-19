package com.wakaztahir.kate.parser.stream

import com.wakaztahir.kate.GlobalModelObjectName
import com.wakaztahir.kate.dsl.ModelObjectImpl
import com.wakaztahir.kate.model.LazyBlock
import com.wakaztahir.kate.model.model.MutableKATEObject
import com.wakaztahir.kate.runtime.GlobalObjectImplementation

class TextParserSourceStream(
    private val sourceCode: String,
    override val model: MutableKATEObject = ModelObjectImpl(objectName = GlobalModelObjectName),
    override val placeholderManager: PlaceholderManager = ParserSourceStream.EmptyPlaceholderManager(),
    override val embeddingManager: EmbeddingManager = ParserSourceStream.NoEmbeddings,
    initialize: Boolean = true
) : ParserSourceStream {

    init {
        if (initialize) {
            DefaultPlaceholderManagerInitializer.initializerDefaultPlaceholders(this)
            GlobalObjectImplementation.putIntoObject(model)
        }
    }

    override var pointer: Int = 0

    override val currentChar: Char
        get() = sourceCode[pointer]

    override val hasEnded get() = sourceCode.length == pointer

    override val block: LazyBlock = ParserSourceStream.ParserBlock(this)

    override fun lookAhead(offset: Int): Char? {
        val position = pointer + offset
        return if (position <= sourceCode.length) {
            sourceCode[position]
        } else {
            null
        }
    }

    override fun lookAhead(offset: Int, length: Int): String? {
        val start = pointer + offset
        val stop = start + length
        return if (stop <= sourceCode.length) {
            sourceCode.substring(start, stop)
        } else {
            null
        }
    }

    override fun incrementPointer(): Boolean {
        return setPointerAt(pointer + 1)
    }

    override fun increment(amount: Int): Boolean {
        return setPointerAt(pointer + amount)
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