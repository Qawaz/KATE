package com.wakaztahir.kate.lexer.model

sealed interface StaticToken : KATEToken {

    data class String(val representation: kotlin.String) : StaticToken

    data class Char(val representation: kotlin.Char) : StaticToken

}