package com.wakaztahir.kate.parser.stream

import com.wakaztahir.kate.lexer.SourceStream
import com.wakaztahir.kate.model.LazyBlock
import com.wakaztahir.kate.model.ModelProvider
import com.wakaztahir.kate.model.PlaceholderParsedBlock
import com.wakaztahir.kate.model.model.MutableKATEObject

interface ParserSourceStream : SourceStream {

    object NoEmbeddings : EmbeddingManager {
        override val embeddedStreams: MutableMap<String, Boolean> = mutableMapOf()
        override fun provideStream(block: LazyBlock, path: String): ParserSourceStream? {
            return null
        }
    }

    class EmptyPlaceholderManager : PlaceholderManager {
        override val placeholders: MutableList<PlaceholderParsedBlock> = mutableListOf()
        override val undefinedPlaceholders: MutableList<PlaceholderParsedBlock> = mutableListOf()
        override val placeholderListeners = mutableMapOf<String, PlaceholderManager.PlaceholderEventListener>()
    }

    class ParserBlock(override val source: ParserSourceStream) : LazyBlock {
        override val provider: ModelProvider = ModelProvider.Lazy { source.model }
        override val isDefaultNoRaw: Boolean
            get() = true
        override val indentationLevel: Int = 0
        override fun canIterate(): Boolean = !source.hasEnded
    }

    val embeddingManager: EmbeddingManager

    val placeholderManager: PlaceholderManager

    val model: MutableKATEObject

    val block: LazyBlock

    fun decrementPointer(decrease: Int = 1): Boolean

    fun setPointerAt(position: Int): Boolean

}