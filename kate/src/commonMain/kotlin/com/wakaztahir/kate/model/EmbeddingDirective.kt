package com.wakaztahir.kate.model

import com.wakaztahir.kate.parser.stream.DestinationStream
import com.wakaztahir.kate.tokenizer.NodeTokenizer

class EmbeddingDirective(val path: String, val embedOnce: Boolean, val block: LazyBlock) : AtDirective {

    override fun <T> selectNode(tokenizer: NodeTokenizer<T>): T = tokenizer.embeddingDirective

    val parsedBlock = if (embedOnce) {
        this.block.source.embeddingManager.embedOnceParseStream(this.block, path)
    } else {
        this.block.source.embeddingManager.embedParseStream(this.block, path)
    }

    override fun generateTo(destination: DestinationStream) {
        parsedBlock?.generateTo(destination)
    }

}