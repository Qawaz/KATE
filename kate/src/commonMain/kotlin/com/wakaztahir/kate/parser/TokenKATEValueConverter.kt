package com.wakaztahir.kate.parser

import com.wakaztahir.kate.lexer.model.ArithmeticOperatorToken
import com.wakaztahir.kate.lexer.model.StaticToken
import com.wakaztahir.kate.lexer.model.TokenConverter
import com.wakaztahir.kate.lexer.tokens.dynamic.AccessChainToken
import com.wakaztahir.kate.lexer.tokens.dynamic.ErrorToken
import com.wakaztahir.kate.lexer.tokens.dynamic.ExpressionToken
import com.wakaztahir.kate.lexer.tokens.dynamic.PrimitiveToken
import com.wakaztahir.kate.model.*
import com.wakaztahir.kate.model.model.KATEParsingError
import com.wakaztahir.kate.model.model.KATEValue
import com.wakaztahir.kate.model.model.ReferencedOrDirectValue

class TokenKATEValueConverter(val provider: ModelProvider) : TokenConverter<ReferencedOrDirectValue> {

    override fun convert(token: StaticToken.String): KATEValue {
        TODO("Not yet implemented")
    }

    override fun convert(token: StaticToken.Char): KATEValue {
        TODO("Not yet implemented")
    }

    override fun convert(token: StaticToken.Whitespace): ReferencedOrDirectValue {
        TODO("Not yet implemented")
    }

    override fun convert(token: PrimitiveToken.BooleanToken): KATEValue {
        return BooleanValue(token.value)
    }

    override fun convert(token: PrimitiveToken.CharToken): KATEValue {
        return CharValue(token.char)
    }

    override fun convert(token: PrimitiveToken.StringToken): KATEValue {
        return StringValue(token.text)
    }

    override fun convert(token: PrimitiveToken.NumberToken.IntToken): KATEValue {
        return IntValue(token.value)
    }

    override fun convert(token: PrimitiveToken.NumberToken.DoubleToken): KATEValue {
        return DoubleValue(token.value)
    }

    override fun convert(token: PrimitiveToken.NumberToken.LongToken): KATEValue {
        return LongValue(token.value)
    }

    override fun convert(token: ArithmeticOperatorToken): ReferencedOrDirectValue {
        TODO("Not yet implemented")
    }

    override fun convert(token: AccessChainToken): ReferencedOrDirectValue {
        return ModelDirective(token.path.map {
            when (it) {
                is AccessChainToken.ChainMemberToken.FunctionCall -> {
                    ModelReference.FunctionCall(name = it.name, it.parameters.map { token ->
                        token.convert(this)
                    })
                }

                is AccessChainToken.ChainMemberToken.Property -> {
                    ModelReference.Property(name = it.name)
                }
            }
        }, provider = provider)
    }

    override fun convert(token: ExpressionToken): ReferencedOrDirectValue {
        return ExpressionValue(
            first = token.first.convert(this),
            operatorType = token.operator,
            second = token.second.convert(this)
        )
    }

    override fun convert(token: ErrorToken): ReferencedOrDirectValue {
        return KATEParsingError(token.exception)
    }

}