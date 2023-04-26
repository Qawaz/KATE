package com.wakaztahir.kate.parser

import com.wakaztahir.kate.model.*
import com.wakaztahir.kate.model.model.KATEObject
import com.wakaztahir.kate.model.model.KATEValue
import com.wakaztahir.kate.model.model.ReferencedOrDirectValue
import com.wakaztahir.kate.parser.stream.SourceStream
import com.wakaztahir.kate.parser.variable.parseVariableReference

internal interface ExpressionValueParser {
    fun SourceStream.parseExpressionValue(): ReferencedOrDirectValue?
}

class DefaultExpressionValueParser(
    private val parseStringAndChar: Boolean,
    private val parseDirectRefs: Boolean
) : ExpressionValueParser {
    override fun SourceStream.parseExpressionValue(): ReferencedOrDirectValue? {
        if (parseStringAndChar) {
            parseStringValue()?.let { return it }
            parseCharacterValue()?.let { return it }
        }
        parseBooleanValue()?.let { return it }
        parseNumberValue()?.let { return it }
        parseVariableReference(parseDirectRefs = parseDirectRefs)?.let { return it }
        return null
    }
}

internal data class ExpressionValue(
    val first: ReferencedOrDirectValue,
    val operatorType: ArithmeticOperatorType,
    val second: ReferencedOrDirectValue
) : ReferencedOrDirectValue {

    override fun getKATEValue(model: KATEObject): KATEValue {
        return first.getKATEValue(model).operate(operatorType,second.getKATEValue(model))
    }

    override fun toString(): String {
        return first.toString() + ' ' + operatorType.char + ' ' + second.toString()
    }

}