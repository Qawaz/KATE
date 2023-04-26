package com.wakaztahir.kate.parser

import com.wakaztahir.kate.model.*
import com.wakaztahir.kate.model.model.KATEObject
import com.wakaztahir.kate.model.model.KATEValue
import com.wakaztahir.kate.model.model.ReferencedOrDirectValue
import com.wakaztahir.kate.parser.variable.parseVariableReference

internal interface ExpressionValueParser {
    fun LazyBlock.parseExpressionValue(): ReferencedOrDirectValue?
}

class DefaultExpressionValueParser(
    private val parseStringAndChar: Boolean,
    private val parseDirectRefs: Boolean
) : ExpressionValueParser {
    override fun LazyBlock.parseExpressionValue(): ReferencedOrDirectValue? {
        if (parseStringAndChar) {
            source.parseStringValue()?.let { return it }
            source.parseCharacterValue()?.let { return it }
        }
        source.parseBooleanValue()?.let { return it }
        source.parseNumberValue()?.let { return it }
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