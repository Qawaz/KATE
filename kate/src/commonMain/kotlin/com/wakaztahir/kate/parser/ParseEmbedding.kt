package com.wakaztahir.kate.parser

import com.wakaztahir.kate.lexer.tokens.StaticTokens
import com.wakaztahir.kate.model.EmbeddingDirective
import com.wakaztahir.kate.model.LazyBlock
import com.wakaztahir.kate.lexer.stream.increment
import com.wakaztahir.kate.lexer.stream.incrementDirective
import com.wakaztahir.kate.lexer.stream.parseTextWhile

fun LazyBlock.parseEmbedding(): EmbeddingDirective? {
    if (source.incrementDirective(StaticTokens.Embed)) {
        val isOnce = source.increment(StaticTokens.UnderscoreOnce)
        if (!source.increment(StaticTokens.SingleSpace)) {
            throw IllegalStateException("there must be a space between @embed / @embed_once and its path")
        }
        return EmbeddingDirective(
            path = source.parseTextWhile { currentChar != '\n' }.ifEmpty {
                throw IllegalStateException("@embed path cannot be empty")
            }.trim().replace("\n", ""),
            embedOnce = isOnce,
            block = this
        )
    }
    return null
}