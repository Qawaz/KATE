package com.wakaztahir.kate.lexer.model

import com.wakaztahir.kate.lexer.stream.SourceStream
import com.wakaztahir.kate.lexer.stream.increment
import com.wakaztahir.kate.lexer.stream.incrementDirective

sealed interface StaticTokenLexer : TokenLexer {

    class Char(private val token: StaticToken.Char, private val onNotFound: () -> KATEToken?) : StaticTokenLexer {
        override fun lex(stream: SourceStream): KATEToken? {
            return if (stream.increment(token.representation)) token else onNotFound()
        }
    }

    class String(private val token: StaticToken.String, private val onNotFound: () -> KATEToken?) : StaticTokenLexer {
        override fun lex(stream: SourceStream): KATEToken? {
            return if (stream.increment(token)) token else onNotFound()
        }
    }

    object Whitespace : StaticTokenLexer {
        override fun lex(stream: SourceStream): KATEToken {
            val previous = stream.pointer
            while (stream.currentChar == ' ') {
                stream.incrementPointer()
            }
            return StaticToken.Whitespace(previous - stream.pointer)
        }
    }

    class Directive(private val token: StaticToken.String, private val onNotFound: () -> KATEToken?) : StaticTokenLexer {
        override fun lex(stream: SourceStream): KATEToken? {
            return if (stream.incrementDirective(token)) token else onNotFound()
        }
    }

}