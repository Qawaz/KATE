package com.wakaztahir.kate.parser.stream

import com.wakaztahir.kate.lexer.SourceStream
import com.wakaztahir.kate.lexer.tokens.CharStaticToken
import com.wakaztahir.kate.lexer.tokens.StaticToken
import com.wakaztahir.kate.lexer.tokens.StaticTokens
import com.wakaztahir.kate.lexer.tokens.StringStaticToken
import com.wakaztahir.kate.model.LazyBlock

internal class UnexpectedEndOfStream(message: String) : Exception(message)

fun SourceStream.increment(char: Char): Boolean {
    return if (!hasEnded && currentChar == char) {
        return incrementPointer()
    } else {
        false
    }
}

internal fun ParserSourceStream.increment(token: StringStaticToken): Boolean {
    return increment(token.representation)
}

fun ParserSourceStream.increment(token: CharStaticToken): Boolean {
    return increment(token.representation)
}

internal fun ParserSourceStream.incrementDirective(second: StringStaticToken): Boolean {
    return if (increment(StaticTokens.AtDirective)) {
        if (increment(second.representation)) {
            true
        } else {
            decrementPointer()
            false
        }
    } else {
        false
    }
}

// TODO fix this one
internal fun ParserSourceStream.incrementDirective(first : StringStaticToken,second : CharStaticToken) : Boolean {
    return increment(StaticTokens.AtDirective.representation + first.representation + second.representation)
}

internal fun ParserSourceStream.increment(first: CharStaticToken, second: StringStaticToken): Boolean {
    return if (increment(first.representation)) {
        if (increment(second.representation)) {
            true
        } else {
            decrementPointer()
            false
        }
    } else {
        false
    }
}

internal fun ParserSourceStream.incrementAndReturnDirective(second: StringStaticToken): String? {
    return if (incrementDirective(second)) {
        StaticTokens.AtDirective.representation + second.representation
    } else null
}

internal fun ParserSourceStream.increment(str: String, throwOnUnexpectedEOS: Boolean = false): Boolean {
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

internal fun ParserSourceStream.parseTextUntilConsumedNew(token: StringStaticToken): String? {
    return parseTextUntilConsumedNew(token.representation)
}

internal fun ParserSourceStream.parseTextUntilConsumedDirectiveNew(
    second: StringStaticToken
): String? {
    return parseTextUntilConsumedNew(StaticTokens.AtDirective.representation + second.representation)
}

internal fun ParserSourceStream.parseTextUntilConsumedNew(str: String): String? {
    var text = ""
    val previous = pointer
    while (!hasEnded) {
        if (currentChar == str[0] && increment(str)) {
            return text
        } else {
            text += currentChar
        }
        incrementPointer()
    }
    decrementPointer(pointer - previous)
    return null
}

internal fun ParserSourceStream.incrementUntilConsumed(token: StringStaticToken): Boolean {
    return incrementUntilConsumed(token.representation)
}


internal fun ParserSourceStream.incrementUntilConsumed(str: String): Boolean {
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

internal fun ParserSourceStream.incrementUntil(token : StringStaticToken) : Boolean {
    return incrementUntil(token.representation)
}

internal fun ParserSourceStream.incrementUntil(str: String): Boolean {
    return if (incrementUntilConsumed(str)) {
        decrementPointer(str.length)
        true
    } else {
        false
    }
}

internal inline fun <T> ParserSourceStream.resetIfNull(perform: ParserSourceStream.() -> T?): T? {
    val previous = pointer
    val value = perform()
    if (value == null) decrementPointer(pointer - previous)
    return value
}

internal fun ParserSourceStream.escapeSpaces() {
    if (increment(StaticTokens.SingleSpace)) escapeSpaces()
}

internal inline fun ParserSourceStream.incrementWhile(block: ParserSourceStream.() -> Boolean) {
    while (!hasEnded) {
        if (!block()) {
            break
        }
        incrementPointer()
    }
}

internal inline fun ParserSourceStream.parseTextWhile(block: ParserSourceStream.() -> Boolean): String {
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

internal fun ParserSourceStream.printLeft() = resetIfNull {
    println(parseTextWhile { true })
    null
}

internal fun ParserSourceStream.printLeftAscii() = resetIfNull {
    for (char in parseTextWhile { true }) println("$char:${char.code}")
    null
}

internal fun ParserSourceStream.parseTextUntilConsumed(token: StringStaticToken): String {
    return parseTextWhile {
        currentChar != token.representation[0] || !increment(token)
    }
}

internal inline fun ParserSourceStream.incrementUntilDirectiveWithSkip(
    directive : StringStaticToken,
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

inline fun ParserSourceStream.readStream(startPointer: Int, limit: Int, block: () -> Unit) {
    val previous = pointer
    setPointerAt(startPointer)
    while (!hasEnded && pointer < limit) {
        block()
        incrementPointer()
    }
    setPointerAt(previous)
}

fun ParserSourceStream.getErrorInfoAtCurrentPointer(): Pair<Int, Int> {
    val pointerAt = pointer
    var lineNumber = 1
    var charIndex = 0
    readStream(0, pointerAt) {
        charIndex++
        if (currentChar == '\n') {
            lineNumber++
            charIndex = 0
        }
    }
    return Pair(lineNumber, charIndex)
}

fun ParserSourceStream.printErrorLineNumberAndCharacterIndex() {
    val errorInfo = getErrorInfoAtCurrentPointer()
    println("Error : Line Number : ${errorInfo.first} , Character Index : ${errorInfo.second}")
    println("Un-parsed Code : ")
    printLeft()
}

internal fun LazyBlock.escapeBlockSpacesForward() {

    val previous = source.pointer
    while (!source.hasEnded) {
        when (source.currentChar) {

            '\r' -> {
                source.incrementPointer()
                if (source.currentChar == '\n') source.incrementPointer()
                return
            }

            '\n' -> {
                source.incrementPointer()
                return
            }

            ' ' -> {
                source.incrementPointer()
                continue
            }

            else -> {
                break
            }
        }
    }

    source.setPointerAt(previous)
    if (source.currentChar == ' ') {
        source.incrementPointer()
        return
    }

}

internal fun LazyBlock.escapeBlockSpacesBackward() {

    val previous = source.pointer
    var currentIndentationLevel = indentationLevel
    while (!source.hasEnded) {
        source.decrementPointer()
        when (source.currentChar) {

            '\r' -> {
                return
            }

            '\n' -> {
                source.decrementPointer()
                if (source.currentChar != '\r') source.incrementPointer()
                return
            }

            ' ' -> {
                continue
            }

            '\t' -> {
                if (currentIndentationLevel > 0) {
                    currentIndentationLevel--
                } else {
                    return
                }
            }

            else -> {
                break
            }
        }
    }


    source.setPointerAt(previous)
    source.decrementPointer()
    if (source.currentChar != ' ') {
        source.incrementPointer()
        return
    }

}

internal fun String.escapeBlockSpacesBackward(indentationLevel: Int): String {
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