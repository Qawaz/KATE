package com.wakaztahir.kate.lexer.model

interface KATEToken {
    fun <T> convert(converter: TokenConverter<T>): T
}