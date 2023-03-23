package com.wakaztahir.kte.parser

import com.wakaztahir.kte.model.model.MutableKTEObject
import com.wakaztahir.kte.model.*
import com.wakaztahir.kte.parser.stream.*
import com.wakaztahir.kte.parser.stream.increment
import com.wakaztahir.kte.parser.stream.unexpected

//-------------- Reference

class VariableReferenceParseException(message: String) : Exception(message)

internal fun SourceStream.parseVariableReference(): ModelDirective? {
    if (currentChar == '@' && increment("@var(")) {
        val propertyPath = mutableListOf<ModelReference>()
        val variableName = parseTextWhile { currentChar.isModelDirectiveLetter() }
        if (variableName.isNotEmpty()) {
            propertyPath.add(ModelReference.Property(variableName))
            if (!increment(')')) {
                throw VariableReferenceParseException("expected ) got $currentChar")
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

internal data class VariableDeclaration(val variableName: String, val variableValue: ReferencedValue) : AtDirective {
    fun storeValue(model: MutableKTEObject) {
        val value = try {
            variableValue.asPrimitive(model)
        } catch (e: Exception) {
            throw e
        }
        model.putValue(variableName, value)
    }

    override fun generateTo(block: LazyBlock, source: SourceStream, destination: DestinationStream) {
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

internal fun SourceStream.parseVariableDeclaration(): VariableDeclaration? {
    val variableName = parseVariableName()
    if (variableName != null) {
        if (variableName.isNotEmpty()) {
            escapeSpaces()
            increment('=')
            escapeSpaces()
            val property = this.parseExpression()
            return if (property != null) {
                VariableDeclaration(variableName = variableName, variableValue = property)
            } else {
                throw VariableDeclarationParseException("constant's value not found")
            }
        } else {
            if (hasEnded) {
                unexpected()
            } else {
                printLeft()
                throw VariableDeclarationParseException("constant's name not given")
            }
        }
    }
    return null
}