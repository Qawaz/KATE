package com.wakaztahir.kte.parser

import com.wakaztahir.kte.model.LazyBlock
import com.wakaztahir.kte.model.PrimitiveValue
import com.wakaztahir.kte.model.ReferencedValue
import com.wakaztahir.kte.model.model.KTEObject
import com.wakaztahir.kte.model.operateAny
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
    return null
}

internal data class ExpressionValue(
    val first: ReferencedValue,
    val operatorType: ArithmeticOperatorType,
    val second: ReferencedValue
) : ReferencedValue {

    override fun asPrimitive(model: KTEObject): PrimitiveValue<*> {
        return first.asPrimitive(model).operateAny(operatorType, second.asPrimitive(model))
    }

    override fun asNullablePrimitive(model: KTEObject): PrimitiveValue<*>? {
        return first.asPrimitive(model).operateAny(operatorType, second.asPrimitive(model))
    }

    override fun stringValue(indentationLevel: Int): String {
        return indentation(indentationLevel) +
                first.stringValue(0) + ' ' + operatorType.char + ' ' + second.stringValue(0)
    }

    override fun toString(): String {
        return stringValue(0)
    }

    override fun generateTo(block: LazyBlock, destination: DestinationStream) {
        asPrimitive(block.model).generateTo(block, destination)
    }

}