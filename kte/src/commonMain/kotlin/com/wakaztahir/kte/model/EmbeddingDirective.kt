package com.wakaztahir.kte.model

import com.wakaztahir.kte.parser.stream.DestinationStream
import com.wakaztahir.kte.parser.stream.SourceStream

class EmbeddingDirective(val path: String) : AtDirective {
    override fun generateTo(block: LazyBlock, source: SourceStream, destination: DestinationStream) {
        TODO("Not yet implemented")
    }

}