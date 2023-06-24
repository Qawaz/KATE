package com.wakaztahir.kate.parser

import com.wakaztahir.kate.lexer.lexers.EmbeddingLexer
import com.wakaztahir.kate.lexer.tokens.StaticTokens
import com.wakaztahir.kate.model.EmbeddingDirective
import com.wakaztahir.kate.model.LazyBlock
import com.wakaztahir.kate.lexer.stream.increment
import com.wakaztahir.kate.lexer.stream.incrementDirective
import com.wakaztahir.kate.lexer.stream.parseTextWhile

fun LazyBlock.parseEmbedding(): EmbeddingDirective? {
    EmbeddingLexer(source).lexEmbeddingOrThrow()?.let {
        return EmbeddingDirective(path = it.path,embedOnce = it.embedOnce,block = this)
    }
    return null
}