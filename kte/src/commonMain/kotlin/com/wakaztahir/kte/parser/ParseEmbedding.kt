package com.wakaztahir.kte.parser

import com.wakaztahir.kte.model.EmbeddingDirective
import com.wakaztahir.kte.parser.stream.SourceStream
import com.wakaztahir.kte.parser.stream.increment
import com.wakaztahir.kte.parser.stream.parseTextWhile

fun SourceStream.parseEmbedding(): EmbeddingDirective? {
    if (currentChar == '@' && increment("@embed")) {
        increment(' ')
        return EmbeddingDirective(
            path = parseTextWhile { currentChar != '\n' }.ifEmpty {
                throw IllegalStateException("@embed path cannot be empty")
            }
        )
    }
    return null
}