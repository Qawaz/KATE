package com.wakaztahir.kate.parser

import com.wakaztahir.kate.lexer.lexers.value.AccessChainLexer
import com.wakaztahir.kate.lexer.lexers.value.DefaultExpressionValueLexer
import com.wakaztahir.kate.lexer.lexers.value.PrimitiveValueLexer
import com.wakaztahir.kate.lexer.tokens.dynamic.PrimitiveToken
import com.wakaztahir.kate.model.*
import com.wakaztahir.kate.model.model.KATEValue
import com.wakaztahir.kate.model.model.ReferencedOrDirectValue
import com.wakaztahir.kate.parser.stream.ParserSourceStream
import com.wakaztahir.kate.parser.variable.parseVariableReference

internal interface ExpressionValueParser {
    fun LazyBlock.parseExpressionValue(): ReferencedOrDirectValue?
}

internal fun ParserSourceStream.parsePrimitiveValue(): PrimitiveValue<*>? {
    PrimitiveValueLexer.lex(this)?.let {
        it.convert(TokenKATEValueConverter(ModelProvider.LateInit())).let { value ->
            return value as PrimitiveValue<*>
        }
    }
    return null
}

class DefaultExpressionValueParser(private val parseDirectRefs: Boolean) : ExpressionValueParser {
    override fun LazyBlock.parseExpressionValue(): ReferencedOrDirectValue? {
        DefaultExpressionValueLexer(parseDirectRefs).lex(source)?.let { return it.convert(TokenKATEValueConverter(provider)) }
//        source.parsePrimitiveValue()?.let { return it }
//        AccessChainLexer(parseDirectRefs = parseDirectRefs).lex(source)?.let { return it.convert(TokenKATEValueConverter(provider)) }
//        parseVariableReference(parseDirectRefs = parseDirectRefs)?.let { return it }
        return null
    }
}

internal data class ExpressionValue(
    val first: ReferencedOrDirectValue,
    val operatorType: ArithmeticOperatorType,
    val second: ReferencedOrDirectValue
) : ReferencedOrDirectValue {

    override fun getKATEValue(): KATEValue {
        return first.getKATEValue().operate(operatorType, second.getKATEValue())
    }

    override fun toString(): String {
        return first.toString() + ' ' + operatorType.char + ' ' + second.toString()
    }

}