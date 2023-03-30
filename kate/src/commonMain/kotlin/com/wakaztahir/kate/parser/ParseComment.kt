package com.wakaztahir.kate.parser

import com.wakaztahir.kate.parser.stream.SourceStream
import com.wakaztahir.kate.parser.stream.increment
import com.wakaztahir.kate.parser.stream.incrementUntilConsumed

internal class CommentParseException(message: String) : Exception(message)

fun SourceStream.skipMultilineComments(): Boolean {
    return if (increment("<%--")) {
        if (!incrementUntilConsumed("--%>")) {
            throw CommentParseException("comment must end with --%>")
        } else {
            true
        }
    } else {
        false
    }
}