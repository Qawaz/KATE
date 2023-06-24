package com.wakaztahir.kate.parser

import com.wakaztahir.kate.lexer.lexers.RuntimeWriteLexer
import com.wakaztahir.kate.lexer.tokens.StaticTokens
import com.wakaztahir.kate.model.CodeGen
import com.wakaztahir.kate.model.LazyBlock
import com.wakaztahir.kate.model.model.ReferencedOrDirectValue
import com.wakaztahir.kate.lexer.stream.increment
import com.wakaztahir.kate.lexer.stream.incrementDirective
import com.wakaztahir.kate.parser.stream.DestinationStream
import com.wakaztahir.kate.tokenizer.NodeTokenizer
import kotlin.jvm.JvmInline

@JvmInline
value class WriteString(val string: ReferencedOrDirectValue) : CodeGen {
    override fun <T> selectNode(tokenizer: NodeTokenizer<T>): T = tokenizer.runtimeWriteString
    override fun generateTo(destination: DestinationStream) {
        string.asNullablePrimitive()?.generateTo(destination)
            ?: throw IllegalStateException("invalid value passed to $${StaticTokens.RuntimeWrite}")
    }
}

fun LazyBlock.parseRuntimeGen(): CodeGen? {
    RuntimeWriteLexer(source).lexWriteValueToken()?.let {
        return WriteString(it.convert(TokenKATEValueConverter(provider)))
    }
    return null
}