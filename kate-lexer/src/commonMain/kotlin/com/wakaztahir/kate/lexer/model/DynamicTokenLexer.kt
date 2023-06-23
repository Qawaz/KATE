package com.wakaztahir.kate.lexer.model

import com.wakaztahir.kate.lexer.stream.SourceStream

interface DynamicTokenLexer<T : DynamicToken> {
    fun lex(stream: SourceStream): T?
}