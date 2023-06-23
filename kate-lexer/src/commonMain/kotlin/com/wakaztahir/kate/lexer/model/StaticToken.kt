package com.wakaztahir.kate.lexer.model

sealed interface StaticToken : KATEToken {

    data class String(val representation: kotlin.String) : StaticToken {
        override fun <T> convert(converter: TokenConverter<T>): T {
            return converter.convert(this)
        }
    }

    data class Char(val representation: kotlin.Char) : StaticToken {
        override fun <T> convert(converter: TokenConverter<T>): T {
            return converter.convert(this)
        }
    }

}