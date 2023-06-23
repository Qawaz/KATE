package com.wakaztahir.kate

import com.wakaztahir.kate.dsl.ModelObjectImpl
import com.wakaztahir.kate.lexer.InputSourceStream
import com.wakaztahir.kate.model.LazyBlock
import com.wakaztahir.kate.model.model.MutableKATEObject
import com.wakaztahir.kate.parser.stream.*
import com.wakaztahir.kate.runtime.GlobalObjectImplementation
import java.io.InputStream

class InputParserSourceStream(
    inputStream: InputStream,
    override val model: MutableKATEObject = ModelObjectImpl(GlobalModelObjectName),
    override val embeddingManager: EmbeddingManager = ParserSourceStream.NoEmbeddings,
    override val placeholderManager: PlaceholderManager = ParserSourceStream.EmptyPlaceholderManager(),
    initialize: Boolean = true
) : InputSourceStream(inputStream), ParserSourceStream {

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