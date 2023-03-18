package com.wakaztahir.kte.parser

import com.wakaztahir.kte.TemplateContext
import com.wakaztahir.kte.parser.stream.increment
import com.wakaztahir.kte.parser.stream.incrementUntil

internal class CommentParseException(message: String) : Throwable(message)

fun TemplateContext.parseComment(): Boolean {
    return if (stream.increment("<%--")) {
        if (!stream.incrementUntil("--%>")) {
            throw CommentParseException("comment must end with --%>")
        } else {
            true
        }
    } else {
        false
    }
}