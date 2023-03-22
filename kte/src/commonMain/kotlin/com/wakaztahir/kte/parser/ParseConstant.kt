package com.wakaztahir.kte.parser

import com.wakaztahir.kte.model.model.MutableTemplateModel
import com.wakaztahir.kte.model.*
import com.wakaztahir.kte.parser.stream.*
import com.wakaztahir.kte.parser.stream.increment
import com.wakaztahir.kte.parser.stream.unexpected

//-------------- Reference

class ConstantReferenceParseException(message: String) : Throwable(message)

internal fun SourceStream.parseConstantReference(): ModelDirective? {
    if (currentChar == '@' && increment("@const(")) {
        val propertyPath = mutableListOf<ModelReference>()
        val variableName = parseTextWhile { currentChar.isModelDirectiveLetter() }
        if (variableName.isNotEmpty()) {
            propertyPath.add(ModelReference.Property(variableName))
            if (!increment(')')) {
                throw ConstantReferenceParseException("expected ) got $currentChar")
            }
        } else {
            throw ConstantReferenceParseException("@const( variable name is empty")
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

class ConstantDeclarationParseException(message: String) : Throwable(message)

internal fun Char.isConstantVariableName(): Boolean = this.isLetterOrDigit() || this == '_'

internal fun SourceStream.parseConstantVariableName(): String? {
    if (currentChar == '@' && increment("@const")) {
        increment(' ')
        return parseTextWhile { currentChar.isConstantVariableName() }
    }
    return null
}

internal fun SourceStream.parseConstantDeclaration(): ConstantDeclaration? {
    val variableName = parseConstantVariableName()
    if (variableName != null) {
        if (variableName.isNotEmpty()) {
            escapeSpaces()
            increment('=')
            escapeSpaces()
            val property = this.parseExpression()
            return if (property != null) {
                ConstantDeclaration(variableName = variableName, variableValue = property)
            } else {
                throw ConstantDeclarationParseException("constant's value not found")
            }
        } else {
            if (hasEnded) {
                unexpected()
            } else {
                printLeft()
                throw ConstantDeclarationParseException("constant's name not given")
            }
        }
    }
    return null
}