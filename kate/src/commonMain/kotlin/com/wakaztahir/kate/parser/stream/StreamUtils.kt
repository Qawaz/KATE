package com.wakaztahir.kate.parser.stream

import com.wakaztahir.kate.lexer.stream.SequentialStream
import com.wakaztahir.kate.lexer.stream.SourceStream
import com.wakaztahir.kate.lexer.stream.escapeBlockSpacesForward
import com.wakaztahir.kate.lexer.stream.printLeft
import com.wakaztahir.kate.model.LazyBlock

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