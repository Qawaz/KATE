package com.wakaztahir.kate.parser.stream

import com.wakaztahir.kate.GlobalModelObjectName
import com.wakaztahir.kate.dsl.ModelObjectImpl
import com.wakaztahir.kate.lexer.stream.TextSourceStream
import com.wakaztahir.kate.model.LazyBlock
import com.wakaztahir.kate.model.model.MutableKATEObject
import com.wakaztahir.kate.runtime.GlobalObjectImplementation

class TextParserSourceStream(
    sourceCode: String,
    override val model: MutableKATEObject = ModelObjectImpl(objectName = GlobalModelObjectName),
    override val placeholderManager: PlaceholderManager = ParserSourceStream.EmptyPlaceholderManager(),
    override val embeddingManager: EmbeddingManager = ParserSourceStream.NoEmbeddings,
    initialize: Boolean = true
) : TextSourceStream(sourceCode = sourceCode), ParserSourceStream {

    init {
        if (initialize) {
            DefaultPlaceholderManagerInitializer.initializerDefaultPlaceholders(this)
            GlobalObjectImplementation.putIntoObject(model)
        }
    }

    override val block: LazyBlock = ParserSourceStream.ParserBlock(this)

    override fun decrementPointer(decrease: Int): Boolean {
        if (decrease == 0) return true
        return super.setStreamPointer(pointer - decrease)
    }

    override fun setPointerAt(position: Int): Boolean {
        return super.setStreamPointer(position)
    }

}