package com.wakaztahir.kte.parser.stream

import com.wakaztahir.kte.model.LazyBlock

interface EmbeddingManager {

    val embeddedStreams: MutableMap<String, Boolean>

    fun provideStream(block: LazyBlock, path: String): SourceStream?

    fun embedGenerateStream(block: LazyBlock, path: String, destination: DestinationStream) {
        val stream = provideStream(block, path)
        if (stream != null) {
            embeddedStreams[path] = true
            stream.generateTo(destination)
        } else {
            throw IllegalStateException("stream with path $path not found")
        }
    }

    fun embedOnceGenerateStream(block: LazyBlock, path: String, destination: DestinationStream) {
        if (embeddedStreams[path] == true) return
        embedGenerateStream(block, path, destination)
    }

}