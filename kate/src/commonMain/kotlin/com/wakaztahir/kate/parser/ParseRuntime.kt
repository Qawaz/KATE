package com.wakaztahir.kate.parser

import com.wakaztahir.kate.model.CharValue
import com.wakaztahir.kate.model.CodeGen
import com.wakaztahir.kate.model.LazyBlock
import com.wakaztahir.kate.model.StringValue
import com.wakaztahir.kate.model.model.ReferencedValue
import com.wakaztahir.kate.parser.stream.DestinationStream
import com.wakaztahir.kate.parser.stream.SourceStream
import com.wakaztahir.kate.parser.stream.increment
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

private fun SourceStream.parseRefCharValue(parseDirectRefs : Boolean): ReferencedValue? {
    parseVariableReference(parseDirectRefs = parseDirectRefs)?.let { return it }
    parseCharacterValue()?.let { return it }
    return null
}

private fun SourceStream.parseRefStringValue(parseDirectRefs : Boolean): ReferencedValue? {
    parseVariableReference(parseDirectRefs = parseDirectRefs)?.let { return it }
    parseStringValue()?.let { return it }
    return null
}

fun LazyBlock.parseRuntimeGen(): CodeGen? {
    if (source.currentChar == '@') {
        if (source.increment(CHAR_DIRECTIVE)) {
            if (!source.increment('(')) throw IllegalStateException("expected '(' got ${source.currentChar}")
            val value =
                source.parseRefCharValue(parseDirectRefs = false) ?: throw IllegalStateException("value for runtime directive not found")
            if (!source.increment(')')) throw IllegalStateException("expected ')' got ${source.currentChar}")
            return WriteChar(value)
        } else if (source.increment(STRING_DIRECTIVE)) {
            if (!source.increment('(')) throw IllegalStateException("expected '(' got ${source.currentChar}")
            val value =
                source.parseRefStringValue(parseDirectRefs = false) ?: throw IllegalStateException("value for runtime directive not found")
            if (!source.increment(')')) throw IllegalStateException("expected ')' got ${source.currentChar}")
            return WriteString(value)
        }
    }
    return null
}