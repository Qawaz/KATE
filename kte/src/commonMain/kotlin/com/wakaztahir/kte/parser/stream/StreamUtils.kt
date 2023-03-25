package com.wakaztahir.kte.parser.stream

internal class UnexpectedEndOfStream(message: String) : Exception(message)

internal fun SourceStream.unexpected(): UnexpectedEndOfStream {
    return UnexpectedEndOfStream("unexpected end of stream at pointer : $pointer")
}

internal fun SourceStream.unexpected(expected: String): UnexpectedEndOfStream {
    return UnexpectedEndOfStream("unexpected end of stream , expected $expected at pointer : $pointer")
}

internal fun SourceStream.increment(char: Char): Boolean {
    return if (!hasEnded && currentChar == char) {
        return incrementPointer()
    } else {
        false
    }
}

internal fun SourceStream.increment(str: String, throwOnUnexpectedEOS: Boolean = false): Boolean {
    require(str.length > 1) {
        println("$str should be more than a single character")
    }
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

internal fun SourceStream.incrementUntilConsumed(str: String): Boolean {
    val previous = pointer
    while (!hasEnded) {
        if (currentChar == str[0] && increment(str)) {
            return true
        }
        incrementPointer()
    }
    decrementPointer(pointer - previous)
    return false
}

internal fun SourceStream.incrementUntil(str: String): Boolean {
    return if (incrementUntilConsumed(str)) {
        decrementPointer(str.length)
        true
    } else {
        false
    }
}

internal inline fun <T> SourceStream.resetIfNull(perform: SourceStream.() -> T?): T? {
    val previous = pointer
    val value = perform()
    if (value == null) decrementPointer(pointer - previous)
    return value
}

internal fun SourceStream.escapeSpaces() {
    if (increment(' ')) {
        escapeSpaces()
    }
}

internal inline fun SourceStream.incrementWhile(block: SourceStream.() -> Boolean) {
    while (!hasEnded) {
        if (!block()) {
            break
        }
        incrementPointer()
    }
}

internal inline fun SourceStream.parseTextWhile(block: SourceStream.() -> Boolean): String {
    var text = ""
    incrementWhile {
        if (block()) {
            text += currentChar
            true
        } else {
            false
        }
    }
    return text
}

internal fun SourceStream.printLeft() = resetIfNull {
    println(parseTextWhile { true })
    null
}

internal fun SourceStream.parseTextUntilConsumed(str: String): String {
    return parseTextWhile {
        currentChar != str[0] || !increment(str)
    }
}

internal inline fun SourceStream.incrementUntilDirectiveWithSkip(
    skip: String,
    canIncrementDirective: () -> String?,
): String? {
    var skips = 0
    while (!hasEnded) {
        if (currentChar == '@') {
            if (increment(skip)) {
                skips++
            } else {
                val incremented = canIncrementDirective()
                if (incremented != null) {
                    if (skips == 0) {
                        return incremented
                    } else {
                        skips--
                    }
                }
            }
        }
        incrementPointer()
    }
    return null
}

internal fun SourceStream.escapeBlockSpacesForward() {

    var pointerAfterFirstSpace = pointer
    if (currentChar == ' ') {
        incrementPointer()
        pointerAfterFirstSpace = pointer
    }

    var foundNewLine = false
    while (!hasEnded) {
        if (currentChar == ' ') {
            incrementPointer()
        } else if (currentChar == '\n') {
            incrementPointer()
            foundNewLine = true
            break
        } else {
            break
        }
    }

    if (!foundNewLine) {
        setPointerAt(pointerAfterFirstSpace)
    }

}

internal fun SourceStream.escapeBlockSpacesBackward() {

    var fallbackPointer = pointer
    if (decrementPointer()) {
        when (currentChar) {
            ' ' -> {
                fallbackPointer = pointer
            }
            '\n' -> {
                return
            }
            else -> {
                incrementPointer()
                return
            }
        }
    }

    var foundNewLine = false
    while (!hasEnded) {
        decrementPointer()
        if (currentChar != ' ') {
            if (currentChar == '\n') {
                foundNewLine = true
                break
            } else {
                incrementPointer()
                break
            }
        }
    }

    if (!foundNewLine) {
        setPointerAt(fallbackPointer)
    }

}