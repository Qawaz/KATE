package com.wakaztahir.kate.parser

import com.wakaztahir.kate.model.*
import com.wakaztahir.kate.model.model.KATEValue
import com.wakaztahir.kate.model.model.ReferencedOrDirectValue
import com.wakaztahir.kate.parser.stream.ParserSourceStream
import com.wakaztahir.kate.parser.variable.parseVariableReference

internal interface ExpressionValueParser {
    fun LazyBlock.parseExpressionValue(): ReferencedOrDirectValue?
}

internal fun ParserSourceStream.parsePrimitiveValue(): PrimitiveValue<*>? {
    parseStringValue()?.let { return it }
    parseCharacterValue()?.let { return it }
    parseBooleanValue()?.let { return it }
    parseNumberValue()?.let { return it }
    return null
}

class DefaultExpressionValueParser(private val parseDirectRefs: Boolean) : ExpressionValueParser {
    override fun LazyBlock.parseExpressionValue(): ReferencedOrDirectValue? {
        source.parsePrimitiveValue()?.let { return it }
        parseVariableReference(parseDirectRefs = parseDirectRefs)?.let { return it }
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