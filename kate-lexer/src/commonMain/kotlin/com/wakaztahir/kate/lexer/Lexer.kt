package com.wakaztahir.kate.lexer

import com.wakaztahir.kate.lexer.stream.SourceStream
import com.wakaztahir.kate.lexer.tokens.KATEToken
import com.wakaztahir.kate.lexer.tokens.TokenRange

interface Lexer {

    val source: SourceStream

    fun lexTokenRangeAtCurrentPosition(): TokenRange?

    fun lexTokenAtCurrentPosition(): KATEToken? {
        return lexTokenRangeAtCurrentPosition()?.token
    }

    fun getState(): Int

    fun restoreState(state : Int)

}