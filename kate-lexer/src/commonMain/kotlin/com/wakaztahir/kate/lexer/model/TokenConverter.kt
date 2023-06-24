package com.wakaztahir.kate.lexer.model

import com.wakaztahir.kate.lexer.tokens.dynamic.AccessChainToken
import com.wakaztahir.kate.lexer.tokens.dynamic.ErrorToken
import com.wakaztahir.kate.lexer.tokens.dynamic.ExpressionToken
import com.wakaztahir.kate.lexer.tokens.dynamic.PrimitiveToken

interface TokenConverter<T> {

    fun convert(token: StaticToken.String): T

    fun convert(token: StaticToken.Char): T

    fun convert(token: PrimitiveToken.BooleanToken): T

    fun convert(token: PrimitiveToken.CharToken): T

    fun convert(token: PrimitiveToken.StringToken): T

    fun convert(token: PrimitiveToken.NumberToken.IntToken): T

    fun convert(token: PrimitiveToken.NumberToken.DoubleToken): T

    fun convert(token: PrimitiveToken.NumberToken.LongToken): T

    fun convert(token: AccessChainToken): T

    fun convert(token: ExpressionToken): T

    fun convert(token: ErrorToken): T

}