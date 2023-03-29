package com.wakaztahir.kte.parser

import com.wakaztahir.kte.model.EmbeddingDirective
import com.wakaztahir.kte.model.LazyBlock
import com.wakaztahir.kte.parser.stream.SourceStream
import com.wakaztahir.kte.parser.stream.increment
import com.wakaztahir.kte.parser.stream.parseTextWhile

fun LazyBlock.parseEmbedding(): EmbeddingDirective? {
    if (source.currentChar == '@' && source.increment("@embed")) {
        val isOnce = source.increment("_once")
        if (!source.increment(' ')) {
            throw IllegalStateException("there must be a space between @embed / @embed_once and its path")
        }
        return EmbeddingDirective(
            path = source.parseTextWhile { currentChar != '\n' }.ifEmpty {
                throw IllegalStateException("@embed path cannot be empty")
            }.trim().replace("\n", ""),
            embedOnce = isOnce
        )
    }
    return null
}