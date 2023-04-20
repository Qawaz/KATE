package com.wakaztahir.kate.parser.variable

import com.wakaztahir.kate.model.model.MutableKATEObject
import com.wakaztahir.kate.model.*
import com.wakaztahir.kate.model.model.ReferencedValue
import com.wakaztahir.kate.parser.parseAnyExpressionOrValue
import com.wakaztahir.kate.parser.parseArithmeticOperator
import com.wakaztahir.kate.parser.stream.*
import com.wakaztahir.kate.parser.stream.increment

class VariableDeclarationException(message: String) : Exception(message)

internal data class VariableDeclaration(val variableName: String, val variableValue: ReferencedValue) : AtDirective {

    override val isEmptyWriter: Boolean
        get() = true

    fun storeValue(model: MutableKATEObject) {
        if (!model.insertValue(variableName, variableValue.getKATEValue(model))) {
            throw VariableDeclarationException("couldn't declare variable $variableName which already exists")
        }
    }

    override fun generateTo(block: LazyBlock, destination: DestinationStream) {
        storeValue(block.model)
    }

}

internal fun SourceStream.parseVariableName(): String? {
    val previous = pointer
    if (currentChar == '@' && increment("@var")) {
        if (currentChar == '(') {
            setPointerAt(previous)
            return null
        }
        increment(' ')
        return parseTextWhile { currentChar.isVariableName() }
    }
    return null
}

internal fun LazyBlock.parseVariableDeclaration(): VariableDeclaration? {
    val variableName = source.parseVariableName()
    if (variableName != null) {
        if (variableName.isNotEmpty()) {
            val valid = isValidVariableName(variableName)
            if (valid.isFailure) throw valid.exceptionOrNull()!!
            source.escapeSpaces()
            if (!source.increment('=')) {
                throw IllegalStateException("expected '=' when assigning a value to variable $variableName but got ${source.currentChar} in variable declaration")
            }
            source.escapeSpaces()
            val property = source.parseAnyExpressionOrValue(
                parseFirstStringOrChar = true,
                parseNotFirstStringOrChar = true,
                parseDirectRefs = true,
                allowAtLessExpressions = true
            )
            return if (property != null) {
                VariableDeclaration(
                    variableName = variableName,
                    variableValue = property
                )
            } else {
                throw VariableDeclarationException("constant's value not found when declaring variable $variableName")
            }
        } else {
            if (source.hasEnded) {
                throw UnexpectedEndOfStream("unexpected end of stream at pointer : ${source.pointer}")
            } else {
                source.printLeft()
                throw VariableDeclarationException("variable's name not given or is empty")
            }
        }
    }
    return null
}