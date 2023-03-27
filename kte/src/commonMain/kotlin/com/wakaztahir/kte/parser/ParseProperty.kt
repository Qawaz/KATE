package com.wakaztahir.kte.parser

import com.wakaztahir.kte.model.*
import com.wakaztahir.kte.model.model.KTEObject
import com.wakaztahir.kte.model.model.KTEValue
import com.wakaztahir.kte.parser.stream.DestinationStream
import com.wakaztahir.kte.parser.stream.SourceStream

internal fun SourceStream.parseNumberReference(): ReferencedValue? {
    parseVariableReference()?.let { return it }
    parseModelDirective()?.let { return it }
    parseNumberValue()?.let { return it }
    return null
}

internal fun SourceStream.parseReferencedValue(): ReferencedValue? {
    parseVariableReference()?.let { return it }
    parseModelDirective()?.let { return it }
    parseListDefinition()?.let { return it }
    parseMutableListDefinition()?.let { return it }
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

    override fun getKTEValue(model: KTEObject): KTEValue {
        return asNullablePrimitive(model)
    }

    override fun stringValue(indentationLevel: Int): String {
        return indentation(indentationLevel) +
                first.stringValue(0) + ' ' + operatorType.char + ' ' + second.stringValue(0)
    }

    override fun toString(): String {
        return stringValue(0)
    }

    override fun generateTo(block: LazyBlock, destination: DestinationStream) {
        asNullablePrimitive(block.model).generateTo(block, destination)
    }

}