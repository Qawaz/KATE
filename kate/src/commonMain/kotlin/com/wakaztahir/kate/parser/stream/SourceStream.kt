package com.wakaztahir.kate.parser.stream

import com.wakaztahir.kate.model.LazyBlock
import com.wakaztahir.kate.model.PlaceholderBlock
import com.wakaztahir.kate.model.model.MutableKATEObject
import com.wakaztahir.kate.runtime.GlobalObjectImplementation

abstract class SourceStream {

    protected object NoEmbeddings : EmbeddingManager {
        override val embeddedStreams: MutableMap<String, Boolean> = mutableMapOf()
        override fun provideStream(block: LazyBlock, path: String): SourceStream? {
            return null
        }
    }

    protected class EmptyPlaceholderManager : PlaceholderManager {
        override val placeholders: MutableList<PlaceholderBlock> = mutableListOf()
        override val undefinedPlaceholders: MutableList<PlaceholderBlock> = mutableListOf()
        override val placeholderListeners = mutableMapOf<String, PlaceholderManager.PlaceholderEventListener>()
    }

    abstract val embeddingManager: EmbeddingManager

    abstract val placeholderManager: PlaceholderManager

    abstract val model : MutableKATEObject

    val block : LazyBlock = object : LazyBlock {

        override val source: SourceStream get() = this@SourceStream

        override val model: MutableKATEObject
            get() = source.model

        override val isWriteUnprocessedTextEnabled: Boolean
            get() = true

        override val indentationLevel: Int = 0

        override fun canIterate(): Boolean = !hasEnded

    }

    abstract val pointer: Int

    abstract val currentChar: Char

    abstract val hasEnded: Boolean

    abstract fun incrementPointer(): Boolean

    abstract fun decrementPointer(decrease: Int = 1): Boolean

    abstract fun setPointerAt(position: Int): Boolean

}