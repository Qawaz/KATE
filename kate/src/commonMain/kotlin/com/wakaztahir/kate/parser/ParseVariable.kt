package com.wakaztahir.kate.parser

import com.wakaztahir.kate.model.model.MutableKATEObject
import com.wakaztahir.kate.model.*
import com.wakaztahir.kate.model.model.KATEValue
import com.wakaztahir.kate.model.model.ReferencedValue
import com.wakaztahir.kate.parser.stream.*
import com.wakaztahir.kate.parser.stream.increment

internal data class VariableDeclaration(
    val variableName: String,
    val arithmeticOperatorType: ArithmeticOperatorType?,
    val variableValue: ReferencedValue
) : AtDirective {

    override val isEmptyWriter: Boolean
        get() = true

    private fun throwIt(): Nothing {
        throw IllegalStateException("error setting value of variable $variableName , couldn't get original value")
    }

    private fun getValue(model: MutableKATEObject): KATEValue {
        return if (arithmeticOperatorType == null) {
            variableValue.getKTEValue(model)
        } else {
            ExpressionValue(
                first = model.getModelReference(ModelReference.Property(variableName)) ?: throwIt(),
                operatorType = arithmeticOperatorType,
                second = variableValue
            ).getKTEValue(model)
        }
    }

    fun storeValue(model: MutableKATEObject) {
        model.putValue(variableName, getValue(model))
    }

    override fun generateTo(block: LazyBlock, destination: DestinationStream) {
        storeValue(block.model)
    }
}

class VariableDeclarationParseException(message: String) : Exception(message)

internal fun Char.isVariableName(): Boolean = this.isLetterOrDigit() || this == '_'

internal fun SourceStream.parseVariableName(): String? {
    if (currentChar == '@' && increment("@var")) {
        increment(' ')
        return parseTextWhile { currentChar.isVariableName() }
    }
    return null
}

private fun isTakenVariableName(name: String): Boolean {
    return when (name) {
        "this" -> true
        "parent" -> true
        else -> false
    }
}

internal fun LazyBlock.parseVariableDeclaration(): VariableDeclaration? {
    val variableName = source.parseVariableName()
    if (variableName != null) {
        if (variableName.isNotEmpty()) {
            if (variableName.first().isDigit()) {
                throw IllegalStateException("variable name cannot start with a digit $variableName")
            }
            if (isTakenVariableName(variableName)) {
                throw IllegalStateException("variable name cannot be $variableName")
            }
            source.escapeSpaces()
            val arithmeticOperator = source.parseArithmeticOperator()
            if (!source.increment('=')) {
                throw IllegalStateException("expected '=' when assigning a value to variable $variableName but got ${source.currentChar} in variable declaration")
            }
            source.escapeSpaces()
            val property = source.parseAnyExpressionOrValue()
            return if (property != null) {
                VariableDeclaration(
                    variableName = variableName,
                    arithmeticOperatorType = arithmeticOperator,
                    variableValue = property
                )
            } else {
                throw VariableDeclarationParseException("constant's value not found when declaring variable $variableName")
            }
        } else {
            if (source.hasEnded) {
                throw UnexpectedEndOfStream("unexpected end of stream at pointer : ${source.pointer}")
            } else {
                source.printLeft()
                throw VariableDeclarationParseException("variable's name not given or is empty")
            }
        }
    }
    return null
}