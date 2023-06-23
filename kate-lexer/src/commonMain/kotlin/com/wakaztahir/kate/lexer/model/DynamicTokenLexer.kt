package com.wakaztahir.kate.lexer.model

import com.wakaztahir.kate.lexer.stream.SourceStream

interface DynamicTokenLexer<T : DynamicToken> : TokenLexer {
    override fun lex(stream: SourceStream): T?
}