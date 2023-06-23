package com.wakaztahir.kate.parser

import com.wakaztahir.kate.lexer.tokens.StaticTokens
import com.wakaztahir.kate.model.LazyBlock
import com.wakaztahir.kate.lexer.stream.*
import com.wakaztahir.kate.lexer.stream.increment
import com.wakaztahir.kate.lexer.stream.incrementUntilConsumed
import com.wakaztahir.kate.parser.stream.ParserSourceStream

internal class CommentParseException(message: String) : Exception(message)

fun LazyBlock.parseMultilineComment(): MultilineComment? {
    return if (source.increment(StaticTokens.CommentStart)) {
        val commentText =
            source.parseTextUntilConsumedNew(StaticTokens.CommentEnd)
                ?: throw CommentParseException("comment must end with ${StaticTokens.CommentEnd}")
        return MultilineComment(commentText)
    } else {
        null
    }
}

fun ParserSourceStream.skipMultilineComments(): Boolean {
    return if (increment(StaticTokens.CommentStart)) {
        if (!incrementUntilConsumed(StaticTokens.CommentEnd)) {
            throw CommentParseException("comment must end with ${StaticTokens.CommentEnd}")
        } else {
            true
        }
    } else {
        false
    }
}