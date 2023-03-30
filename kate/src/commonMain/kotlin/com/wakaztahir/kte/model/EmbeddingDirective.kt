package com.wakaztahir.kte.model

import com.wakaztahir.kte.parser.stream.DestinationStream

class EmbeddingDirective(val path: String,val embedOnce : Boolean) : AtDirective {
    override fun generateTo(block: LazyBlock, destination: DestinationStream) {
        if(embedOnce) {
            block.source.embeddingManager.embedOnceGenerateStream(block, path, destination)
        }else {
            block.source.embeddingManager.embedGenerateStream(block, path, destination)
        }
    }

}