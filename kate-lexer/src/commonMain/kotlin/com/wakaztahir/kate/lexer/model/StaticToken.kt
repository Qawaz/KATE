package com.wakaztahir.kate.lexer.model

sealed interface StaticToken : KATEToken {

    data class StringStaticToken(val representation: String) : StaticToken

    data class CharStaticToken(val representation: Char) : StaticToken

}

typealias CharStaticToken = StaticToken.CharStaticToken

typealias StringStaticToken = StaticToken.StringStaticToken