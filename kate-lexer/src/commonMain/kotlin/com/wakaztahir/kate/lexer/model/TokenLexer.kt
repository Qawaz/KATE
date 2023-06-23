package com.wakaztahir.kate.lexer.model

import com.wakaztahir.kate.lexer.stream.SourceStream

interface TokenLexer {

    fun lex(stream: SourceStream): KATEToken?

}