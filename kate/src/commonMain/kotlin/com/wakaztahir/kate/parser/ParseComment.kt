package com.wakaztahir.kate.parser

import com.wakaztahir.kate.model.CodeGen
import com.wakaztahir.kate.model.LazyBlock
import com.wakaztahir.kate.parser.stream.*
import com.wakaztahir.kate.parser.stream.increment
import com.wakaztahir.kate.parser.stream.incrementUntilConsumed
import com.wakaztahir.kate.tokenizer.NodeTokenizer

internal class CommentParseException(message: String) : Exception(message)

fun LazyBlock.parseMultilineComment(): MultilineComment? {
    return if (source.increment("<%--")) {
        val commentText = source.parseTextUntilConsumedNew("--%>") ?: throw CommentParseException("comment must end with --%>")
        return MultilineComment(commentText)
    } else {
        null
    }
}

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