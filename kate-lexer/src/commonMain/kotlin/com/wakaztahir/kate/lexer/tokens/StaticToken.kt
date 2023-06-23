package com.wakaztahir.kate.lexer.tokens

sealed interface StaticToken : KATEToken {

    data class StringStaticToken(val representation: String) : StaticToken

    data class CharStaticToken(val representation: Char) : StaticToken

}

typealias StringStaticToken = StaticToken.StringStaticToken

typealias CharStaticToken = StaticToken.CharStaticToken