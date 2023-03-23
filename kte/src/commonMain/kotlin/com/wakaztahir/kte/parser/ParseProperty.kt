package com.wakaztahir.kte.parser

import com.wakaztahir.kte.model.LazyBlock
import com.wakaztahir.kte.model.PrimitiveValue
import com.wakaztahir.kte.model.ReferencedValue
import com.wakaztahir.kte.model.model.KTEObject
import com.wakaztahir.kte.parser.stream.DestinationStream
import com.wakaztahir.kte.parser.stream.SourceStream
import com.wakaztahir.kte.parser.stream.increment

internal fun SourceStream.parseReferencedValue(): ReferencedValue? {
    parseVariableReference()?.let { return it }
    parseModelDirective()?.let { return it }
    return null
}

private fun SourceStream.parseDynamicProperty(): ReferencedValue? {
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

    override fun generateTo(block: LazyBlock, source: SourceStream, destination: DestinationStream) {
        asPrimitive(block.model).generateTo(block, source, destination)
    }
}

internal fun SourceStream.parseExpression(): ReferencedValue? {
    val firstValue = parseDynamicProperty()
    if (firstValue != null) {
        val spaced = increment(' ')
        return if (increment('@')) {
            val arithmeticOperator = parseArithmeticOperator()
            if (arithmeticOperator != null) {
                increment(' ')
                val secondValue = parseExpression()
                if (secondValue != null) {
                    if (secondValue is ExpressionValue) {
                        if (arithmeticOperator.precedence < secondValue.operatorType.precedence) {
                            return secondValue.copy(
                                first = ExpressionValue(
                                    first = firstValue,
                                    operatorType = arithmeticOperator,
                                    second = secondValue.first
                                )
                            )
                        }
                    }
                    return ExpressionValue(
                        first = firstValue,
                        operatorType = arithmeticOperator,
                        second = secondValue
                    )
                } else {
                    throw IllegalStateException("expected second value number / property / variable instead got $currentChar")
                }
            } else {
                decrementPointer(1)
                firstValue
            }
        } else {
            if (spaced) decrementPointer(1)
            firstValue
        }
    }
    return null
}