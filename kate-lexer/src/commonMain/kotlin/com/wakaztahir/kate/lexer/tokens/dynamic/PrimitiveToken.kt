package com.wakaztahir.kate.lexer.tokens.dynamic

import com.wakaztahir.kate.lexer.model.DynamicToken
import com.wakaztahir.kate.lexer.model.TokenConverter

sealed interface PrimitiveToken : ValueToken {

    data class BooleanToken(val value: Boolean) : PrimitiveToken {
        override fun <T> convert(converter: TokenConverter<T>): T {
            return converter.convert(this)
        }
    }

    data class CharToken(val char: Char) : PrimitiveToken {
        override fun <T> convert(converter: TokenConverter<T>): T {
            return converter.convert(this)
        }
    }

    data class StringToken(val text: String) : PrimitiveToken {
        override fun <T> convert(converter: TokenConverter<T>): T {
            return converter.convert(this)
        }
    }

    sealed interface NumberToken : PrimitiveToken {

        data class IntToken(val value: Int) : NumberToken {
            override fun <T> convert(converter: TokenConverter<T>): T {
                return converter.convert(this)
            }
        }

        data class DoubleToken(val value: Double) : NumberToken {
            override fun <T> convert(converter: TokenConverter<T>): T {
                return converter.convert(this)
            }
        }

        data class LongToken(val value: Long) : NumberToken {
            override fun <T> convert(converter: TokenConverter<T>): T {
                return converter.convert(this)
            }
        }

    }

}