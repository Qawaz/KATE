package com.wakaztahir.kate.parser

import com.wakaztahir.kate.lexer.tokens.StaticTokens
import com.wakaztahir.kate.model.CodeGen
import com.wakaztahir.kate.model.LazyBlock
import com.wakaztahir.kate.model.model.ReferencedOrDirectValue
import com.wakaztahir.kate.parser.stream.DestinationStream
import com.wakaztahir.kate.parser.stream.increment
import com.wakaztahir.kate.parser.stream.incrementDirective
import com.wakaztahir.kate.tokenizer.NodeTokenizer
import kotlin.jvm.JvmInline

@JvmInline
value class WriteChar(val char: ReferencedOrDirectValue) : CodeGen {
    override fun <T> selectNode(tokenizer: NodeTokenizer<T>): T = tokenizer.runtimeWriteChar
    override fun generateTo(destination: DestinationStream) {
        destination.stream.write(
            (char.asNullablePrimitive()?.value as? Char)
                ?: throw IllegalStateException("passed value to $${StaticTokens.RuntimeWriteChar} is not a character")
        )
    }
}

@JvmInline
value class WriteString(val string: ReferencedOrDirectValue) : CodeGen {
    override fun <T> selectNode(tokenizer: NodeTokenizer<T>): T = tokenizer.runtimeWriteString
    override fun generateTo(destination: DestinationStream) {
        destination.stream.write(
            (string.asNullablePrimitive()?.value as? String)
                ?: throw IllegalStateException("passed value to ${StaticTokens.RuntimeWriteString} is not a string")
        )
    }
}

fun LazyBlock.parseRuntimeGen(): CodeGen? {
    if (source.incrementDirective(StaticTokens.RuntimeWriteChar)) {
        if (!source.increment(StaticTokens.LeftParenthesis)) throw IllegalStateException("expected '(' got ${source.currentChar}")
        val value = parseExpression(
            parseDirectRefs = true
        ) ?: throw IllegalStateException("value for runtime directive not found")
        if (!source.increment(StaticTokens.RightParenthesis)) throw IllegalStateException("expected ')' got ${source.currentChar}")
        return WriteChar(value)
    } else if (source.incrementDirective(StaticTokens.RuntimeWriteString)) {
        if (!source.increment(StaticTokens.LeftParenthesis)) throw IllegalStateException("expected '(' got ${source.currentChar}")
        val value = parseExpression(
            parseDirectRefs = true
        ) ?: throw IllegalStateException("value for runtime directive not found")
        if (!source.increment(StaticTokens.RightParenthesis)) throw IllegalStateException("expected ')' got ${source.currentChar}")
        return WriteString(value)
    }
    return null
}