package com.wakaztahir.kate.parser

import com.wakaztahir.kate.model.*
import com.wakaztahir.kate.model.model.KATEObject
import com.wakaztahir.kate.model.model.KATEValue
import com.wakaztahir.kate.parser.stream.SourceStream
import com.wakaztahir.kate.parser.variable.parseVariableReference

internal interface ExpressionValueParser {
    fun SourceStream.parseExpressionValue(): KATEValue?
}

class DefaultExpressionValueParser(
    private val parseStringAndChar: Boolean,
    private val parseDirectRefs: Boolean
) : ExpressionValueParser {
    override fun SourceStream.parseExpressionValue(): KATEValue? {
        if (parseStringAndChar) {
            parseStringValue()?.let { return it }
            parseCharacterValue()?.let { return it }
        }
        parseNumberValue()?.let { return it }
        parseVariableReference(parseDirectRefs = parseDirectRefs)?.let { return it }
        return null
    }
}

internal data class ExpressionValue(
    val first: KATEValue,
    val operatorType: ArithmeticOperatorType,
    val second: KATEValue
) : KATEValue {

    override fun asNullablePrimitive(model: KATEObject): PrimitiveValue<*> {
        return first.asNullablePrimitive(model)?.let { first ->
            second.asNullablePrimitive(model)?.let { second ->
                first.operateAny(operatorType, second)
            } ?: run {
                throw IllegalStateException("second value in expression $this is not a primitive")
            }
        } ?: run {
            throw IllegalStateException("first value in expression $this is not a primitive")
        }
    }

    override fun getModelReference(reference: ModelReference): KATEValue? {
        return null
    }

    override fun getKATEType(model: KATEObject): KATEType {
        return getKATEValue(model).getKATEType(model)
    }

    override fun compareTo(model: KATEObject, other: KATEValue): Int {
        return asNullablePrimitive(model).compareTo(model, other)
    }

    override fun getKATEValue(model: KATEObject): KATEValue {
        return asNullablePrimitive(model)
    }

    override fun toString(): String {
        return first.toString() + ' ' + operatorType.char + ' ' + second.toString()
    }

}