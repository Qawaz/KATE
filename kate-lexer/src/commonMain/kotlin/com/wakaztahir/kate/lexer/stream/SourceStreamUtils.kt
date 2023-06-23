package com.wakaztahir.kate.lexer.stream

import com.wakaztahir.kate.lexer.stream.SourceStream
import com.wakaztahir.kate.lexer.tokens.CharStaticToken
import com.wakaztahir.kate.lexer.tokens.StaticTokens
import com.wakaztahir.kate.lexer.tokens.StringStaticToken

class UnexpectedEndOfStream(message: String) : Exception(message)

fun SourceStream.increment(text: String, throwOnUnexpectedEOS: Boolean = false): Boolean {
    require(text.length > 1) {
        println("$text should be more than a single character")
    }
    return lookAhead {
        val previous = pointer
        while (!hasEnded && pointer - previous < text.length) {
            val current = pointer - previous
            if (text[current] != currentChar) {
                restorePosition()
                return@lookAhead false
            } else {
                if (!incrementPointer()) {
                    break
                }
            }
        }
        val current = pointer - previous
        if (current == text.length) {
            true
        } else {
            if (throwOnUnexpectedEOS) {
                throw UnexpectedEndOfStream("unexpected end of stream , expected $text")
            } else {
                restorePosition()
                false
            }
        }
    }
}

fun SourceStream.increment(token: StringStaticToken): Boolean {
    return increment(token.representation)
}

fun SourceStream.incrementDirective(second: StringStaticToken): Boolean {
    return lookAhead {
        if (increment(StaticTokens.AtDirective)) {
            if (increment(second.representation)) {
                true
            } else {
                restorePosition()
                false
            }
        } else {
            false
        }
    }
}

// TODO fix this one
fun SourceStream.incrementDirective(first: StringStaticToken, second: CharStaticToken): Boolean {
    return increment(StaticTokens.AtDirective.representation + first.representation + second.representation)
}

fun SourceStream.increment(first: CharStaticToken, second: StringStaticToken): Boolean {
    return lookAhead {
        if (increment(first.representation)) {
            if (increment(second.representation)) {
                true
            } else {
                restorePosition()
                false
            }
        } else {
            false
        }
    }
}

fun SourceStream.incrementAndReturnDirective(second: StringStaticToken): String? {
    return if (incrementDirective(second)) {
        StaticTokens.AtDirective.representation + second.representation
    } else null
}


fun SourceStream.parseTextUntilConsumedNew(token: StringStaticToken): String? {
    return parseTextUntilConsumedNew(token.representation)
}

fun SourceStream.parseTextUntilConsumedDirectiveNew(
    second: StringStaticToken
): String? {
    return parseTextUntilConsumedNew(StaticTokens.AtDirective.representation + second.representation)
}

fun SourceStream.parseTextUntilConsumedNew(text: String): String? {
    var parsedText = ""
    return lookAhead {
        while (!hasEnded) {
            if (currentChar == text[0] && increment(text)) {
                return@lookAhead parsedText
            } else {
                parsedText += currentChar
            }
            incrementPointer()
        }
        restorePosition()
        null
    }
}

fun SourceStream.incrementUntilConsumed(token: StringStaticToken): Boolean {
    return incrementUntilConsumed(token.representation)
}


fun SourceStream.incrementUntilConsumed(str: String): Boolean {
    return lookAhead {
        while (!hasEnded) {
            if (currentChar == str[0] && increment(str)) {
                return@lookAhead true
            }
            incrementPointer()
        }
        restorePosition()
        false
    }
}

fun SourceStream.incrementUntil(str: String): Boolean {
    return lookAhead {
        val previous = pointer
        if (incrementUntilConsumed(str)) {
            restoreIncrementing(pointer - previous - str.length)
            true
        } else {
            false
        }
    }
}

fun SourceStream.incrementUntil(token: StringStaticToken): Boolean {
    return incrementUntil(token.representation)
}

inline fun <T> SourceStream.resetIfNull(crossinline perform: SourceStream.() -> T?): T? {
    return lookAhead {
        val value = perform()
        if (value == null) restorePosition()
        value
    }
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

fun SourceStream.printLeft() = resetIfNull {
    println(parseTextWhile { true })
    null
}

fun SourceStream.printLeftAscii() = resetIfNull {
    for (char in parseTextWhile { true }) println("$char:${char.code}")
    null
}

fun SourceStream.parseTextUntilConsumed(token: StringStaticToken): String {
    return parseTextWhile {
        currentChar != token.representation[0] || !increment(token)
    }
}

inline fun SourceStream.incrementUntilDirectiveWithSkip(
    directive: StringStaticToken,
    canIncrementDirective: (skips: Int) -> String?,
): String? {
    var skips = 0
    while (!hasEnded) {
        if (currentChar == StaticTokens.AtDirective.representation) {
            if (incrementDirective(directive)) {
                skips++
                continue
            } else {
                val incremented = canIncrementDirective(skips)
                if (incremented != null) {
                    if (skips == 0) {
                        return incremented
                    } else {
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