package com.wakaztahir.kte.parser

import com.wakaztahir.kte.model.PrimitiveValue
import com.wakaztahir.kte.model.ReferencedValue
import com.wakaztahir.kte.model.model.KTEObject
import com.wakaztahir.kte.parser.stream.LanguageDestination
import com.wakaztahir.kte.parser.stream.SourceStream
import com.wakaztahir.kte.parser.stream.increment

internal fun SourceStream.parseReferencedValue(): ReferencedValue? {
    parseVariableReference()?.let { return it }
    parseModelDirective()?.let { return it }
    return null
}

fun SourceStream.parseDynamicProperty(): ReferencedValue? {
    parseReferencedValue()?.let { return it }
    parsePrimitiveValue()?.let { return it }
    return null
}

internal data class ExpressionValue(
    val first: ReferencedValue,
    val operatorType: ArithmeticOperatorType,
    val second: ReferencedValue
) : ReferencedValue {

    override fun asPrimitive(model: KTEObject): PrimitiveValue<*> {
        return first.asPrimitive(model).operate(operatorType, second.asPrimitive(model))
    }

    override fun stringValue(indentationLevel: Int): String {
        return indentation(indentationLevel) +
                first.stringValue(0) + ' ' + operatorType.char + ' ' + second.stringValue(0)
    }

    override fun toString(): String {
        return stringValue(0)
    }

    override fun writeTo(model: KTEObject, destination: LanguageDestination) {
        asPrimitive(model).writeTo(model, destination)
    }

}