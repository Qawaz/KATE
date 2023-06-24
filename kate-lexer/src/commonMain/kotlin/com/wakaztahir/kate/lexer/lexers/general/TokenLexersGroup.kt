package com.wakaztahir.kate.lexer.lexers.general

import com.wakaztahir.kate.lexer.Lexer
import com.wakaztahir.kate.lexer.model.KATEToken
import com.wakaztahir.kate.lexer.model.TokenLexer
import com.wakaztahir.kate.lexer.stream.SourceStream
import com.wakaztahir.kate.lexer.tokens.dynamic.ErrorToken
import kotlin.jvm.JvmInline

open class TokenLexersGroup(
    override val source: SourceStream,
    private val lexers: List<LexerFactory>,
) : Lexer {

    private var index = 0
    private var errorToken: ErrorToken? = null

    init {
        require(lexers.isNotEmpty())
    }

    private val scope: ContinuationScope = object : ContinuationScope {
        override fun stopLexing() {
            index = lexers.size
        }

        override fun stopLexingWithError(error: ErrorToken) {
            errorToken = error
        }

        override fun stopLexingWithError(error: Throwable) {
            errorToken = ErrorToken(
                exception = error,
                lineNumber = source.lineNumber,
                columnNumber = source.columnNumber
            )
        }
    }

    private fun getLexerAt(index: Int): TokenLexer {
        return lexers[index].createLexer(scope)
    }

    private fun tokenNotFound(): KATEToken? {
        return null
    }

    override fun lexAndIncrementToken(): KATEToken? {
        return if (index < lexers.size) {
            if (errorToken != null) {
                index = lexers.size
                return errorToken
            }
            val token = getLexerAt(index).lex(source) ?: tokenNotFound()
            index++
            token
        } else null
    }

    fun lexTokens(): List<KATEToken>? {
        val list = mutableListOf<KATEToken>()
        while (index < lexers.size) lexAndIncrementToken()?.let { list.add(it) } ?: return null
        if (list.isEmpty()) return null
        return list
    }

    override fun getState(): Int {
        return index
    }

    override fun restoreState(state: Int) {
        this.index = state
    }

    sealed interface LexerFactory {

        fun createLexer(scope: ContinuationScope): TokenLexer

        @JvmInline
        value class New(private val provider: (ContinuationScope) -> TokenLexer) : LexerFactory {
            override fun createLexer(scope: ContinuationScope): TokenLexer {
                return provider(scope)
            }
        }

    }

    interface ContinuationScope {

        fun stopLexing()

        fun stopLexingWithError(error: ErrorToken)

        fun stopLexingWithError(error: Throwable)

    }

}