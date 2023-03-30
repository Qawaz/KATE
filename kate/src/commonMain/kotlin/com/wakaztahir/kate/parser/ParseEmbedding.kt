package com.wakaztahir.kate.parser

import com.wakaztahir.kate.model.EmbeddingDirective
import com.wakaztahir.kate.model.LazyBlock
import com.wakaztahir.kate.parser.stream.increment
import com.wakaztahir.kate.parser.stream.parseTextWhile

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