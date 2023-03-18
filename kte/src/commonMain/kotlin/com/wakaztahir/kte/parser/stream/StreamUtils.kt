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

internal inline fun <T> SourceStream.resetIfNull(perform: SourceStream.() -> T?): T? {
    val previous = pointer
    val value = perform()
    if (value == null) decrementPointer(pointer - previous)
    return value
}

internal inline fun <T> SourceStream.resetIfNullWithText(
    condition: () -> Boolean,
    perform: SourceStream.(String) -> T?
): T? {
    return resetIfNull {
        var text = ""
        while (!hasEnded && condition()) {
            text += currentChar
            incrementPointer()
        }
        perform(text)
    }
}

internal fun SourceStream.printLeft() = resetIfNullWithText(condition = { true }) { text ->
    println(text)
    null
}

internal fun SourceStream.escapeSpaces() {
    while (!hasEnded) {
        if (currentChar != ' ') {
            break
        }
        incrementPointer()
    }
}

internal fun SourceStream.parseTextWhile(block: SourceStream.() -> Boolean): String {
    var text = ""
    while (!hasEnded) {
        if (block()) {
            text += currentChar
        } else {
            return text
        }
        incrementPointer()
    }
    return text
}

internal fun SourceStream.parseTextUntil(char: Char): String {
    return parseTextWhile { currentChar != char }
}

internal fun SourceStream.parseTextUntil(str: String): String {
    return parseTextWhile { currentChar != str[0] || !increment(str) }
}