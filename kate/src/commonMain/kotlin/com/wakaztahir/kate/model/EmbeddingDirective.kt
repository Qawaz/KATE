package com.wakaztahir.kate.model

import com.wakaztahir.kate.parser.stream.DestinationStream
import com.wakaztahir.kate.tokenizer.NodeTokenizer

class EmbeddingDirective(val path: String,val embedOnce : Boolean) : AtDirective {
    override fun <T> selectNode(tokenizer: NodeTokenizer<T>): T = tokenizer.embeddingDirective
    override fun generateTo(block: LazyBlock, destination: DestinationStream) {
        if(embedOnce) {
            block.source.embeddingManager.embedOnceGenerateStream(block, path, destination)
        }else {
            block.source.embeddingManager.embedGenerateStream(block, path, destination)
        }
    }

}