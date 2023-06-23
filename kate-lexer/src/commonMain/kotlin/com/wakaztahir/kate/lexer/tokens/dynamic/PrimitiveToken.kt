package com.wakaztahir.kate.lexer.tokens.dynamic

import com.wakaztahir.kate.lexer.model.DynamicToken

sealed interface PrimitiveToken : DynamicToken {

    data class BooleanToken(val value: Boolean) : PrimitiveToken

    data class CharToken(val char: Char) : PrimitiveToken

    data class StringToken(val text: String) : PrimitiveToken

    sealed interface NumberToken : PrimitiveToken {

        data class IntToken(val value: Int) : NumberToken

        data class DoubleToken(val value: Double) : NumberToken

        data class LongToken(val value: Long) : NumberToken

    }

}