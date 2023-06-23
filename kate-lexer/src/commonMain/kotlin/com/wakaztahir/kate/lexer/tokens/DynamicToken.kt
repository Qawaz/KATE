package com.wakaztahir.kate.lexer.tokens

sealed interface DynamicToken {

    sealed interface PrimitiveToken : DynamicToken {
        val content : String
    }

    data class DynamicStringToken(override val content : String) : PrimitiveToken

    sealed interface DynamicNumberToken : PrimitiveToken

    data class DynamicIntToken(override val content : String) : DynamicNumberToken

    data class DynamicFloatToken(override val content : String) : DynamicNumberToken



}