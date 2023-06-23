package com.wakaztahir.kate.lexer.lexers

import com.wakaztahir.kate.lexer.Lexer
import com.wakaztahir.kate.lexer.model.KATEToken
import com.wakaztahir.kate.lexer.model.TokenLexer
import com.wakaztahir.kate.lexer.stream.SourceStream

open class TokenLexersGroup(
    override val source: SourceStream,
    private val lexers: List<TokenLexer>,
//    private val onTokenNotFound: TokenNotFoundScope.() -> Unit = { stopLexing() },
) : Lexer {

    private var index = 0

    init {
        require(lexers.isNotEmpty())
    }

    private fun tokenNotFound(): KATEToken? {
        var fallback: KATEToken? = null
//        val scope = object : TokenNotFoundScope {
//            override val index: Int = this@TokenLexersGroup.index
//            override val currentLexer: TokenLexer get() = lexers[index]
//            override val total: Int = lexers.size
//            override fun stopLexing() {
//                this@TokenLexersGroup.index = lexers.size
//            }
//
//            override fun useToken(token: KATEToken) {
//                fallback = token
//            }
//        }
//        onTokenNotFound(scope)
        return fallback
    }

    override fun lexAndIncrementToken(): KATEToken? {
        return if (index < lexers.size) {
            val token = lexers[index].lex(source)
            index++
            token ?: tokenNotFound()
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

//    interface TokenNotFoundScope {
//
//        val index: Int
//
//        val total: Int
//
//        val currentLexer : TokenLexer
//
//        fun stopLexing()
//
//        fun useToken(token: KATEToken)
//
//    }

}