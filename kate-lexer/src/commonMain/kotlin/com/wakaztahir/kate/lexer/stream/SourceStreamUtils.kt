package com.wakaztahir.kate.lexer.stream

import com.wakaztahir.kate.lexer.model.StaticToken
import com.wakaztahir.kate.lexer.tokens.StaticTokens

class UnexpectedEndOfStream(message: String) : Exception(message)

fun SourceStream.isAtCurrentPosition(text: String, offset: Int = 0, throwOnUnexpectedEOS: Boolean = false): Boolean {
    require(text.length > 1) {
        println("$text should be more than a single character")
    }
    var x = 0
//    if (lookAhead(0 + offset) == text[0]) { // unsure about this , condition works
    while (x < text.length) {
        lookAhead(x + offset)?.let {
            if (text[x] != it) {
                return false
            } else {
                x++
            }
        } ?: break
    }
//    }
    return if (x == text.length) true else {
        if (throwOnUnexpectedEOS) {
            throw UnexpectedEndOfStream("unexpected end of stream , expected $text")
        } else {
            false
        }
    }
}


@Suppress("NOTHING_TO_INLINE")
inline fun SourceStream.isAtCurrentPosition(
    token: StaticToken.String,
    offset: Int = 0,
    throwOnUnexpectedEOS: Boolean = false
): Boolean {
    return isAtCurrentPosition(token.representation, offset = offset, throwOnUnexpectedEOS = throwOnUnexpectedEOS)
}

fun SourceStream.increment(text: String, throwOnUnexpectedEOS: Boolean = false): Boolean {
    return if (isAtCurrentPosition(text = text, throwOnUnexpectedEOS = throwOnUnexpectedEOS)) {
        incrementPointer(amount = text.length)
        true
    } else {
        false
    }
}

fun SourceStream.increment(token: StaticToken.String): Boolean {
    return increment(token.representation)
}

fun SourceStream.isAtCurrentPosition(first: StaticToken.Char, second: StaticToken.String): Boolean {
    return if (isAtCurrentPosition(first)) {
        isAtCurrentPosition(second, offset = 1)
    } else false
}

fun SourceStream.increment(first: StaticToken.Char, second: StaticToken.String): Boolean {
    return if (isAtCurrentPosition(first, second)) {
        incrementPointer(amount = second.representation.length + 1)
        true
    } else false
}

fun SourceStream.isDirectiveAtCurrentPosition(second: StaticToken.String): Boolean {
    return if (isAtCurrentPosition(StaticTokens.AtDirective)) {
        isAtCurrentPosition(second, offset = 1)
    } else {
        false
    }
}

fun SourceStream.incrementDirective(second: StaticToken.String): Boolean {
    return if (isDirectiveAtCurrentPosition(second)) {
        incrementPointer(amount = second.representation.length + 1)
        true
    } else {
        false
    }
}

// TODO fix this one
fun SourceStream.incrementDirective(first: StaticToken.String, second: StaticToken.Char): Boolean {
    return increment(StaticTokens.AtDirective.representation + first.representation + second.representation)
}

fun SourceStream.returnDirectiveAtCurrentPosition(second: StaticToken.String): String? {
    return if (isDirectiveAtCurrentPosition(second)) {
        StaticTokens.AtDirective.representation + second.representation
    } else null
}

fun SourceStream.parseTextUntilConsumedDirectiveNew(
    second: StaticToken.String
): String? {
    return parseTextUntilConsumedNew(StaticTokens.AtDirective.representation + second.representation)
}

fun SourceStream.readTextAheadUntil(char: Char): String? {
    var parsedText = ""
    var x = 0
    do {
        val currChar = lookAhead(x)
        if (currChar == char) {
            return parsedText
        } else {
            parsedText += currChar
        }
        x++
    } while (currChar != null)
    return null
}

fun SourceStream.readTextAheadUntil(text: String): String? {
    var parsedText = ""
    var x = 0
    do {
        val currChar = lookAhead(x)
        if (currChar == text[0] && isAtCurrentPosition(text, offset = x)) {
            return parsedText
        } else {
            parsedText += currChar
        }
        x++
    } while (currChar != null)
    return null
}

fun SourceStream.parseTextUntilConsumedNew(stopper: String): String? {
    val text = readTextAheadUntil(text = stopper)
    return if (text != null) {
        incrementPointer(text.length + stopper.length)
        text
    } else {
        null
    }
}

fun SourceStream.parseTextUntilConsumedNew(token: StaticToken.String): String? {
    return parseTextUntilConsumedNew(token.representation)
}

fun SourceStream.parseTextUntil(stopper: StaticToken.String): String? {
    val text = readTextAheadUntil(text = stopper.representation)
    return if (text != null) {
        incrementPointer(text.length)
        text
    } else null
}


fun SourceStream.incrementUntilConsumed(token: StaticToken.String): Boolean {
    return incrementUntilConsumed(token.representation)
}

fun SourceStream.incrementUntil(text: String): Boolean {
    var x = 0
    do {
        val currChar = lookAhead(x)
        if (currChar == text[0] && isAtCurrentPosition(text, offset = x)) {
            incrementPointer(x)
            return true
        }
        x++
    } while (currChar != null)
    return false
}

fun SourceStream.incrementUntilConsumed(text: String): Boolean {
    return if (incrementUntil(text)) {
        incrementPointer(text.length)
        true
    } else {
        false
    }
}

fun SourceStream.incrementUntil(token: StaticToken.String): Boolean {
    return incrementUntil(token.representation)
}

fun SourceStream.escapeSpaces() {
    if (increment(StaticTokens.SingleSpace)) escapeSpaces()
}

inline fun SourceStream.incrementWhile(block: SourceStream.() -> Boolean) {
    while (!hasEnded) {
        if (!block()) {
            break
        }
        incrementPointer()
    }
}

inline fun SourceStream.parseTextWhile(block: SourceStream.() -> Boolean): String {
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

fun SourceStream.readAllAheadFromCurrentPosition(): String? {
    var parsedText = ""
    var x = 0
    do {
        val currChar = lookAhead(x)
        parsedText += currChar
        x++
    } while (currChar != null)
    return null
}

fun SourceStream.printLeft() {
    println(readAllAheadFromCurrentPosition())
}

fun SourceStream.printLeftAscii() {
    val text = readAllAheadFromCurrentPosition()
    if (text != null) {
        for (char in text) println("$char:${char.code}")
    }
}

fun SourceStream.parseTextUntilConsumed(token: StaticToken.String): String {
    return parseTextWhile {
        currentChar != token.representation[0] || !increment(token)
    }
}

inline fun SourceStream.incrementUntilDirectiveWithSkip(
    directive: StaticToken.String,
    returnEnderAtCurrentPosition: (skips: Int) -> String?,
): String? {
    var skips = 0
    while (!hasEnded) {
        if (currentChar == StaticTokens.AtDirective.representation) {
            if (incrementDirective(directive)) {
                skips++
                continue
            } else {
                val ender = returnEnderAtCurrentPosition(skips)
                if (ender != null) {
                    if (skips == 0) {
                        return ender
                    } else {
                        incrementPointer(ender.length)
                        skips--
                        continue
                    }
                }
            }
        }
        incrementPointer()
    }
    return null
}

fun SourceStream.escapeBlockSpacesForward() {

    var x = 0
    do {
        val currChar = lookAhead(x)
        when (currChar) {
            '\r' -> {
                incrementPointer(x + 1)
                if (currentChar == '\n') incrementPointer()
                return
            }

            '\n' -> {
                incrementPointer(x + 1)
                return
            }

            ' ' -> {
                x++
            }

            else -> break
        }
    } while (currChar != null)

    if (currentChar == ' ') {
        incrementPointer()
        return
    }

}

fun String.escapeBlockSpacesBackward(indentationLevel: Int): String {
    var currentIndentationLevel = indentationLevel
    var i = length
    while (i > 0) {
        i--
        when (this[i]) {

            '\r' -> {
                return substring(0, i)
            }

            '\n' -> {
                if (this[i - 1] == '\r') i--
                return substring(0, i)
            }

            ' ' -> {
                continue
            }

            '\t' -> {
                if (currentIndentationLevel > 0) {
                    currentIndentationLevel--
                } else {
                    return substring(0, i)
                }
            }

            else -> {
                break
            }
        }
    }

    i = length - 1
    if (this[i] == ' ') {
        return substring(0, i)
    }

    return this

}