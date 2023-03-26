package com.wakaztahir.kte.parser

import com.wakaztahir.kte.model.model.MutableKTEObject
import com.wakaztahir.kte.model.*
import com.wakaztahir.kte.model.model.KTEFunction
import com.wakaztahir.kte.model.model.KTEValue
import com.wakaztahir.kte.parser.stream.*
import com.wakaztahir.kte.parser.stream.increment
import com.wakaztahir.kte.parser.stream.unexpected

//-------------- Reference

class VariableReferenceParseException(message: String) : Exception(message)

internal fun LazyBlock.parseVariableReference(): ModelDirective? {
    if (source.currentChar == '@' && source.increment("@var(")) {
        val propertyPath = mutableListOf<ModelReference>()
        val variableName = source.parseTextWhile { currentChar.isModelDirectiveLetter() }
        if (variableName.isNotEmpty()) {
            propertyPath.add(ModelReference.Property(variableName))
            if (!source.increment(')')) {
                throw VariableReferenceParseException("expected ) got ${source.currentChar}")
            }
        } else {
            throw VariableReferenceParseException("@var( variable name is empty")
        }
        parseDotReferencesInto(propertyPath)
        return ModelDirective(propertyPath)
    }
    return null
}

//-------------- Declaration

internal data class VariableDeclaration(val variableName: String, val variableValue: KTEValue) : AtDirective {

    override val isEmptyWriter: Boolean
        get() = true

    fun storeValue(model: MutableKTEObject) {
        if (variableValue is KTEFunction) {
            model.putValue(variableName, variableValue.invoke(model, parameters = variableValue.parameters))
        } else {
            model.putValue(variableName, variableValue)
        }
    }

    override fun generateTo(block: LazyBlock, destination: DestinationStream) {
        storeValue(block.model)
    }
}

class VariableDeclarationParseException(message: String) : Exception(message)

internal fun Char.isVariableName(): Boolean = this.isLetterOrDigit() || this == '_'

internal fun LazyBlock.parseVariableName(): String? {
    if (source.currentChar == '@' && source.increment("@var")) {
        source.increment(' ')
        return source.parseTextWhile { currentChar.isVariableName() }
    }
    return null
}

internal fun LazyBlock.parseVariableDeclaration(): VariableDeclaration? {
    val variableName = parseVariableName()
    if (variableName != null) {
        if (variableName.isNotEmpty()) {
            source.escapeSpaces()
            source.increment('=')
            source.escapeSpaces()
            val property = this.parseAnyExpressionOrValue()
            return if (property != null) {
                VariableDeclaration(variableName = variableName, variableValue = property)
            } else {
                throw VariableDeclarationParseException("constant's value not found")
            }
        } else {
            if (source.hasEnded) {
                source.unexpected()
            } else {
                source.printLeft()
                throw VariableDeclarationParseException("constant's name not given")
            }
        }
    }
    return null
}