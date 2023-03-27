package com.wakaztahir.kte.parser

import com.wakaztahir.kte.model.CharValue
import com.wakaztahir.kte.model.CodeGen
import com.wakaztahir.kte.model.LazyBlock
import com.wakaztahir.kte.model.StringValue
import com.wakaztahir.kte.model.model.ReferencedValue
import com.wakaztahir.kte.parser.stream.DestinationStream
import com.wakaztahir.kte.parser.stream.SourceStream
import com.wakaztahir.kte.parser.stream.increment
import kotlin.jvm.JvmInline

private const val CHAR_DIRECTIVE = "@runtime.print_char"
private const val STRING_DIRECTIVE = "@runtime.print_string"

@JvmInline
private value class WriteChar(val char: ReferencedValue) : CodeGen {
    override fun generateTo(block: LazyBlock, destination: DestinationStream) {
        destination.stream.write(
            (char.asNullablePrimitive(block.model) as? CharValue)?.value
                ?: throw IllegalStateException("passed value to $CHAR_DIRECTIVE is not a character")
        )
    }
}

@JvmInline
private value class WriteString(val string: ReferencedValue) : CodeGen {
    override fun generateTo(block: LazyBlock, destination: DestinationStream) {
        destination.stream.write(
            (string.asNullablePrimitive(block.model) as? StringValue)?.value
                ?: throw IllegalStateException("passed value to $STRING_DIRECTIVE is not a string")
        )
    }
}

private fun SourceStream.parseRefCharValue() : ReferencedValue? {
    parseReferencedValue()?.let { return it }
    parseCharacterValue()?.let { return it }
    return null
}

private fun SourceStream.parseRefStringValue() : ReferencedValue? {
    parseReferencedValue()?.let { return it }
    parseStringValue()?.let { return it }
    return null
}

fun LazyBlock.parseRuntimeGen(): CodeGen? {
    if (source.currentChar == '@') {
        if (source.increment(CHAR_DIRECTIVE)) {
            if (!source.increment('(')) throw IllegalStateException("expected '(' got ${source.currentChar}")
            val value =
                source.parseRefCharValue() ?: throw IllegalStateException("value for runtime directive not found")
            if (!source.increment(')')) throw IllegalStateException("expected ')' got ${source.currentChar}")
            return WriteChar(value)
        } else if (source.increment(STRING_DIRECTIVE)) {
            if (!source.increment('(')) throw IllegalStateException("expected '(' got ${source.currentChar}")
            val value =
                source.parseRefStringValue() ?: throw IllegalStateException("value for runtime directive not found")
            if (!source.increment(')')) throw IllegalStateException("expected ')' got ${source.currentChar}")
            return WriteString(value)
        }
    }
    return null
}