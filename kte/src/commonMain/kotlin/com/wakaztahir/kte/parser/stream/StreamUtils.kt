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

internal fun SourceStream.increment(
    until: () -> Boolean,
    stopIf: (Char) -> Boolean
): Boolean {
    val previous = pointer
    while (!hasEnded) {
        if (until()) {
            return true
        }
        if (stopIf(currentChar)) {
            decrementPointer(pointer - previous)
            return false
        }
        incrementPointer()
    }
    decrementPointer(pointer - previous)
    return false
}

internal fun SourceStream.incrementUntil(
    str: String,
    stopIf: (Char) -> Boolean = { false }
): Boolean {
    return increment(until = { currentChar == str[0] && increment(str) }, stopIf = stopIf)
}

internal inline fun SourceStream.withAutoReset(perform: SourceStream.() -> Unit) {
    val previous = pointer
    perform()
    decrementPointer(pointer - previous)
}

//internal inline fun <T> SourceStream.withNullableReset(perform: SourceStream.() -> T?): T? {
//    val previous = pointer
//    val value = perform()
//    if (value == null) decrementPointer(pointer - previous)
//    return value
//}

internal fun SourceStream.printLeft() {
    withAutoReset {
        var text = ""
        while (!hasEnded) {
            text += currentChar
            incrementPointer()
        }
        println(text)
    }
}

internal fun SourceStream.escapeSpaces() {
    while (!hasEnded) {
        if (currentChar != ' ') {
            break
        }
        incrementPointer()
    }
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