package com.wakaztahir.kte.parser.stream

internal class UnexpectedEndOfStream(message: String) : Throwable(message)

internal fun SourceStream.unexpected(): UnexpectedEndOfStream {
    return UnexpectedEndOfStream("unexpected end of stream at pointer : $pointer")
}

internal fun SourceStream.unexpected(expected: String): UnexpectedEndOfStream {
    return UnexpectedEndOfStream("unexpected end of stream , expected $expected at pointer : $pointer")
}

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
            throw UnexpectedEndOfStream("unexpected end of stream , expected $str")
        } else {
            decrementPointer(current)
            false
        }
    }
}

internal fun SourceStream.incrementUntil(chars: Iterable<Char>, allowed: Iterable<Char>): Boolean {
    while (!hasEnded) {
        for (char in chars) {
            if (char == currentChar) {
                return true
            }
        }
        if (!allowed.contains(currentChar)) {
            return false
        }
        incrementPointer()
    }
    return false
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

internal fun SourceStream.parseTextUntil(vararg chars: Char): String {
    var text = ""
    while (!hasEnded) {
        for (char in chars) {
            if (char == currentChar) {
                return text
            }
        }
        text += currentChar
        incrementPointer()
    }
    return text
}

internal fun SourceStream.parseTextUntil(char: Char): String {
    var text = ""
    while (!hasEnded) {
        if (currentChar == char) {
            return text
        } else {
            text += currentChar
        }
        incrementPointer()
    }
    return text
}

internal fun SourceStream.parseTextUntil(str: String): String {
    var text = ""
    while (!hasEnded) {
        if (currentChar == str[0] && increment(str)) {
            return text
        } else {
            text += currentChar
        }
        incrementPointer()
    }
    return text
}