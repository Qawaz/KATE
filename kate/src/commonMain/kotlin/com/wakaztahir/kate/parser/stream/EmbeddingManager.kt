package com.wakaztahir.kate.parser.stream

import com.wakaztahir.kate.model.LazyBlock
import com.wakaztahir.kate.parser.block.ParsedBlock

interface EmbeddingManager {

    val embeddedStreams: MutableMap<String, Boolean>

    fun provideStream(block: LazyBlock, path: String): SourceStream?

    fun handleException(path : String,stream: SourceStream, exception: Throwable) : Nothing {
        throw exception
    }

    fun embedParseStream(block: LazyBlock, path: String): ParsedBlock {
        val stream = provideStream(block, path)
        if (stream != null) {
            embeddedStreams[path] = true
            try {
                return stream.block.parse()
            } catch (e: Throwable) {
                handleException(path,stream, e)
            }
        } else {
            throw IllegalStateException("stream not found with path $path")
        }
    }

    fun embedOnceParseStream(block: LazyBlock, path: String): ParsedBlock? {
        if (embeddedStreams[path] == true) return null
        return embedParseStream(block, path)
    }

}