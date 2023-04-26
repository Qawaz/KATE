package com.wakaztahir.kate.parser.stream

import com.wakaztahir.kate.model.LazyBlock

interface EmbeddingManager {

    val embeddedStreams: MutableMap<String, Boolean>

    fun provideStream(block: LazyBlock, path: String): SourceStream?

    fun handleException(path : String,stream: SourceStream, exception: Throwable) {
        throw exception
    }

    fun embedGenerateStream(block: LazyBlock, path: String, destination: DestinationStream) {
        val stream = provideStream(block, path)
        if (stream != null) {
            embeddedStreams[path] = true
            try {
                stream.block.generateTo(destination)
            } catch (e: Throwable) {
                handleException(path,stream, e)
            }
        } else {
            throw IllegalStateException("stream not found with path $path")
        }
    }

    fun embedOnceGenerateStream(block: LazyBlock, path: String, destination: DestinationStream) {
        if (embeddedStreams[path] == true) return
        embedGenerateStream(block, path, destination)
    }

}