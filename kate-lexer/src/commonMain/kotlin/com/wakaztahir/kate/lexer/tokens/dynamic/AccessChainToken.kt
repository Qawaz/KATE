package com.wakaztahir.kate.lexer.tokens.dynamic

import com.wakaztahir.kate.lexer.model.TokenConverter

class AccessChainToken(val path: List<ChainMemberToken>) : ValueToken {

    override fun <T> convert(converter: TokenConverter<T>): T {
        return converter.convert(this)
    }

    sealed interface ChainMemberToken {

        val name: String

        class Property(override val name: String) : ChainMemberToken {
            override fun toString(): String {
                return name
            }
        }

        class FunctionCall(
            override val name: String,
            val parameters: List<ValueToken>
        ) : ChainMemberToken {
            override fun toString(): String {
                return name + '(' + parameters.joinToString(",") + ')'
            }
        }
    }

}