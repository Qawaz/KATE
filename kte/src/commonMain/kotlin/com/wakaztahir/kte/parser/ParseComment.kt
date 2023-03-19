package com.wakaztahir.kte.parser

import com.wakaztahir.kte.TemplateContext
import com.wakaztahir.kte.parser.stream.SourceStream
import com.wakaztahir.kte.parser.stream.increment
import com.wakaztahir.kte.parser.stream.incrementUntil
import com.wakaztahir.kte.parser.stream.incrementUntilConsumed

internal class CommentParseException(message: String) : Throwable(message)

fun SourceStream.parseComment(): Boolean {
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