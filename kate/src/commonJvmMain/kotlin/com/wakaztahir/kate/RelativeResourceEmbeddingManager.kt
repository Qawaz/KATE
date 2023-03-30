package com.wakaztahir.kate

import com.wakaztahir.kate.model.LazyBlock
import com.wakaztahir.kate.parser.stream.EmbeddingManager
import com.wakaztahir.kate.parser.stream.SourceStream
import com.wakaztahir.kate.parser.stream.getErrorInfoAtCurrentPointer
import com.wakaztahir.kate.parser.stream.printErrorLineNumberAndCharacterIndex

open class RelativeResourceEmbeddingManager(
    private val basePath: String,
    private val classLoader: Class<Any> = object {}.javaClass
) : EmbeddingManager {

    override val embeddedStreams: MutableMap<String, Boolean> = mutableMapOf()

    override fun handleException(path: String, stream: SourceStream, exception: Throwable) {
        val indo = stream.getErrorInfoAtCurrentPointer()
        throw Throwable("$path:${indo.first}:${indo.second}", cause = exception)
    }

    override fun provideStream(block: LazyBlock, path: String): SourceStream? {
        val actualPath = "$basePath/${path.removePrefix("/").removePrefix("./")}"
        val file = classLoader.getResource(actualPath)
            ?: throw IllegalStateException("embedding with path not found $actualPath")
        return InputSourceStream(
            inputStream = file.openStream(),
            model = block.model,
            embeddingManager = RelativeResourceEmbeddingManager(
                basePath = basePath + if (path.contains('/')) path.substring(0, path.lastIndexOf('/')) else "",
                classLoader = classLoader
            ),
            placeholderManager = block.source.placeholderManager
        )
    }

}