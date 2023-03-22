package com.wakaztahir.kte.parser

import com.wakaztahir.kte.model.model.MutableTemplateModel
import com.wakaztahir.kte.model.*
import com.wakaztahir.kte.parser.stream.*
import com.wakaztahir.kte.parser.stream.increment
import com.wakaztahir.kte.parser.stream.unexpected

//-------------- Reference

class VariableReferenceParseException(message: String) : Throwable(message)

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

internal data class ConstantDeclaration(val variableName: String, val variableValue: ReferencedValue) : AtDirective,
    DeclarationStatement {

    override fun storeValue(model: MutableTemplateModel) {
        model.putValue(variableName, variableValue)
    }

    override fun generateTo(block: LazyBlock, source: SourceStream, destination: DestinationStream) {
        storeValue(block.model)
    }
}

class VariableDeclarationParseException(message: String) : Throwable(message)

internal fun Char.isVariableName(): Boolean = this.isLetterOrDigit() || this == '_'

internal fun SourceStream.parseVariableName(): String? {
    if (currentChar == '@' && increment("@var")) {
        increment(' ')
        return parseTextWhile { currentChar.isVariableName() }
    }
    return null
}

internal fun SourceStream.parseVariableDeclaration(): ConstantDeclaration? {
    val variableName = parseVariableName()
    if (variableName != null) {
        if (variableName.isNotEmpty()) {
            escapeSpaces()
            increment('=')
            escapeSpaces()
            val property = this.parseExpression()
            return if (property != null) {
                ConstantDeclaration(variableName = variableName, variableValue = property)
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