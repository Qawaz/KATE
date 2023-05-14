package com.wakaztahir.kate.parser

import com.wakaztahir.kate.model.CodeGen
import com.wakaztahir.kate.model.LazyBlock
import com.wakaztahir.kate.model.model.ReferencedOrDirectValue
import com.wakaztahir.kate.parser.stream.DestinationStream
import com.wakaztahir.kate.parser.stream.increment
import com.wakaztahir.kate.tokenizer.NodeTokenizer
import kotlin.jvm.JvmInline

private const val CHAR_DIRECTIVE = "@runtime.print_char"
private const val STRING_DIRECTIVE = "@runtime.print_string"

@JvmInline
value class WriteChar(val char: ReferencedOrDirectValue) : CodeGen {
    override fun <T> selectNode(tokenizer: NodeTokenizer<T>): T = tokenizer.runtimeWriteChar
    override fun generateTo(destination: DestinationStream) {
        destination.stream.write(
            (char.asNullablePrimitive()?.value as? Char)
                ?: throw IllegalStateException("passed value to $CHAR_DIRECTIVE is not a character")
        )
    }
}

@JvmInline
value class WriteString(val string: ReferencedOrDirectValue) : CodeGen {
    override fun <T> selectNode(tokenizer: NodeTokenizer<T>): T = tokenizer.runtimeWriteString
    override fun generateTo(destination: DestinationStream) {
        destination.stream.write(
            (string.asNullablePrimitive()?.value as? String)
                ?: throw IllegalStateException("passed value to $STRING_DIRECTIVE is not a string")
        )
    }
}

fun LazyBlock.parseRuntimeGen(): CodeGen? {
    if (source.currentChar == '@') {
        if (source.increment(CHAR_DIRECTIVE)) {
            if (!source.increment('(')) throw IllegalStateException("expected '(' got ${source.currentChar}")
            val value = parseExpression(
                parseDirectRefs = true
            ) ?: throw IllegalStateException("value for runtime directive not found")
            if (!source.increment(')')) throw IllegalStateException("expected ')' got ${source.currentChar}")
            return WriteChar(value)
        } else if (source.increment(STRING_DIRECTIVE)) {
            if (!source.increment('(')) throw IllegalStateException("expected '(' got ${source.currentChar}")
            val value = parseExpression(
                parseDirectRefs = true
            ) ?: throw IllegalStateException("value for runtime directive not found")
            if (!source.increment(')')) throw IllegalStateException("expected ')' got ${source.currentChar}")
            return WriteString(value)
        }
    }
    return null
}