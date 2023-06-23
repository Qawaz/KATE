package com.wakaztahir.kate.lexer.stream

import com.wakaztahir.kate.lexer.model.StaticToken

fun SequentialStream.increment(char: Char): Boolean {
    return if (!hasEnded && currentChar == char) {
        return incrementPointer()
    } else {
        false
    }
}

@Suppress("NOTHING_TO_INLINE")
inline fun SequentialStream.increment(token: StaticToken.CharStaticToken): Boolean {
    return increment(token.representation)
}