package com.wakaztahir.kate.lexer.tokens

interface StaticToken : KATEToken

data class StringStaticToken(val representation: String) : StaticToken

data class CharStaticToken(val representation : Char)

