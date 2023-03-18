package com.wakaztahir.kte.parser

class UnexpectedEndOfStream(message: String) : Throwable(message)

internal fun SourceStream.increment(str: String, throwOnUnexpectedEOS: Boolean = false): Boolean {
    val previous = pointer
    while (!hasEnded && pointer - previous < str.length) {
        val current = pointer - previous
        if (str[current] != currentChar) {
            if (!decrementPointer(current)) {
                break
            }
            return false
        } else {
            if (!incrementPointer()) {
                break
            }
        }
    }
    val current = pointer - previous
    return if (current == str.length) {
        true
    } else {
        if (throwOnUnexpectedEOS) {
            throw UnexpectedEndOfStream("Unexpected EOF")
        } else {
            decrementPointer(current)
            false
        }
    }
}

internal fun SourceStream.incrementUntil(str: String): Boolean {
    while (!hasEnded) {
        if (currentChar == str[0] && increment(str)) {
            return true
        }
        incrementPointer()
    }
    return false
}

internal fun SourceStream.parseTextUntil(str: String): String? {
    var text = ""
    while (!hasEnded) {
        if (currentChar == str[0] && increment(str)) {
            return text
        } else {
            text += currentChar
        }
        incrementPointer()
    }
    return null
}