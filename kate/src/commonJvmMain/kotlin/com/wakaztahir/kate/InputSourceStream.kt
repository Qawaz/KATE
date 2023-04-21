package com.wakaztahir.kate

import com.wakaztahir.kate.dsl.ModelObjectImpl
import com.wakaztahir.kate.model.KATEType
import com.wakaztahir.kate.model.LazyBlock
import com.wakaztahir.kate.model.model.MutableKATEObject
import com.wakaztahir.kate.parser.stream.*
import com.wakaztahir.kate.runtime.GlobalObjectImplementation
import java.io.File
import java.io.InputStream

class InputSourceStream(
    private val inputStream: InputStream,
    override val model: MutableKATEObject = ModelObjectImpl(GlobalModelObjectName, itemType = KATEType.Any()),
    override val embeddingManager: EmbeddingManager = NoEmbeddings,
    override val placeholderManager: PlaceholderManager = EmptyPlaceholderManager()
) : SourceStream() {

    class RelativeFileEmbeddingManager(private val file: File) : EmbeddingManager {
        override val embeddedStreams: MutableMap<String, Boolean> = mutableMapOf()
        override fun provideStream(block: LazyBlock, path: String): SourceStream {
            val resolved = file.resolve(path.removePrefix("./"))
            if (!resolved.exists()) throw IllegalStateException("file path doesn't exist ${resolved.absolutePath}")
            return InputSourceStream(
                inputStream = resolved.inputStream(),
                model = block.source.model,
                embeddingManager = RelativeFileEmbeddingManager(file.parentFile),
                placeholderManager = block.source.placeholderManager
            )
        }
    }

    init {
        DefaultPlaceholderManagerInitializer.initializerDefaultPlaceholders(this)
        GlobalObjectImplementation.putIntoObject(model)
    }

    override var pointer: Int = 0

    private val currentInt: Int
        get() {
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