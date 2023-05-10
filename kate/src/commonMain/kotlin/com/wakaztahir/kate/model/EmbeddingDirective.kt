package com.wakaztahir.kate.model

import com.wakaztahir.kate.model.model.MutableKATEObject
import com.wakaztahir.kate.parser.stream.DestinationStream
import com.wakaztahir.kate.parser.stream.EmbeddingManager
import com.wakaztahir.kate.tokenizer.NodeTokenizer

class EmbeddingDirective(val path: String,val embedOnce : Boolean,val block: LazyBlock) : AtDirective {
    override fun <T> selectNode(tokenizer: NodeTokenizer<T>): T = tokenizer.embeddingDirective
    override fun generateTo(model: MutableKATEObject, destination: DestinationStream) {
        if(embedOnce) {
            this.block.source.embeddingManager.embedOnceGenerateStream(this.block, path, destination)
        }else {
            this.block.source.embeddingManager.embedGenerateStream(this.block, path, destination)
        }
    }

}