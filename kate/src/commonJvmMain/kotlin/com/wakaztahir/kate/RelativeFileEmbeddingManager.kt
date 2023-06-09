package com.wakaztahir.kate

import com.wakaztahir.kate.model.LazyBlock
import com.wakaztahir.kate.parser.stream.EmbeddingManager
import com.wakaztahir.kate.parser.stream.ParserSourceStream
import java.io.BufferedInputStream
import java.io.File

@Suppress("unused")
class RelativeFileEmbeddingManager(
    private val file: File,
    override val embeddedStreams: MutableMap<String, Boolean> = mutableMapOf()
) : EmbeddingManager {
    override fun provideStream(block: LazyBlock, path: String): ParserSourceStream {
        val resolved = file.resolve(path.removePrefix("./"))
        if (!resolved.exists()) throw IllegalStateException("file path doesn't exist ${resolved.absolutePath} where resolving $path and parent is $file")
        return InputParserSourceStream(
            inputStream = BufferedInputStream(resolved.inputStream()),
            model = block.source.model,
            embeddingManager = RelativeFileEmbeddingManager(
                file = resolved.parentFile,
                embeddedStreams = embeddedStreams
            ),
            placeholderManager = block.source.placeholderManager,
            initialize = false
        )
    }
}