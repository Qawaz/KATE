package com.wakaztahir.kate.lexer.stream

import com.wakaztahir.kate.lexer.model.StaticToken

@Suppress("NOTHING_TO_INLINE")
inline fun SequentialStream.isAtCurrentPosition(char: Char): Boolean {
    return !hasEnded && currentChar == char
}

fun SequentialStream.increment(char: Char): Boolean {
    return if (isAtCurrentPosition(char = char)) {
        return incrementPointer()
    } else {
        false
    }
}

@Suppress("NOTHING_TO_INLINE")
inline fun SequentialStream.isAtCurrentPosition(token: StaticToken.Char): Boolean {
    return !hasEnded && currentChar == token.representation
}

@Suppress("NOTHING_TO_INLINE")
inline fun SequentialStream.increment(token: StaticToken.Char): Boolean {
    return increment(token.representation)
}