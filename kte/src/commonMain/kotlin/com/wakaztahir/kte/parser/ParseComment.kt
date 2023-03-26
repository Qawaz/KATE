package com.wakaztahir.kte.parser

import com.wakaztahir.kte.model.LazyBlock
import com.wakaztahir.kte.parser.stream.SourceStream
import com.wakaztahir.kte.parser.stream.increment
import com.wakaztahir.kte.parser.stream.incrementUntilConsumed

internal class CommentParseException(message: String) : Exception(message)

fun LazyBlock.skipMultilineComments(): Boolean {
    return if (source.increment("<%--")) {
        if (!source.incrementUntilConsumed("--%>")) {
            throw CommentParseException("comment must end with --%>")
        } else {
            true
        }
    } else {
        false
    }
}