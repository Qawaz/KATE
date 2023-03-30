package com.wakaztahir.kate.parser

import com.wakaztahir.kate.model.*
import com.wakaztahir.kate.model.model.KTEObject
import com.wakaztahir.kate.model.model.KTEValue
import com.wakaztahir.kate.model.model.ReferencedValue
import com.wakaztahir.kate.parser.stream.SourceStream

internal fun SourceStream.parseNumberReference(): ReferencedValue? {
    parseVariableReference()?.let { return it }
    parseNumberValue()?.let { return it }
    return null
}

internal data class ExpressionValue(
    val first: ReferencedValue,
    val operatorType: ArithmeticOperatorType,
    val second: ReferencedValue
) : ReferencedValue {

    override fun asNullablePrimitive(model: KTEObject): PrimitiveValue<*> {
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

    override fun compareTo(model: KTEObject, other: KTEValue): Int {
        return asNullablePrimitive(model).compareTo(model, other)
    }

    override fun getKTEValue(model: KTEObject): KTEValue {
        return asNullablePrimitive(model)
    }

    override fun toString(): String {
        return first.toString() + ' ' + operatorType.char + ' ' + second.toString()
    }

}