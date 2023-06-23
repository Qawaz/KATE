package com.wakaztahir.kate.lexer

import com.wakaztahir.kate.lexer.stream.SourceStream
import com.wakaztahir.kate.lexer.model.KATEToken
import com.wakaztahir.kate.lexer.model.TokenRange

interface Lexer {

    val source: SourceStream

    fun lexAndIncrementToken(): KATEToken?

    fun lexTokenRangeWithoutIncrementing(): TokenRange? = source.lookAhead {
        val start = source.pointer
        val token = lexAndIncrementToken() ?: return@lookAhead null
        val end = source.pointer
        restorePosition()
        TokenRange(token = token, start = start, end = end)
    }

    fun getState(): Int

    fun restoreState(state: Int)

}