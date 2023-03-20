package com.wakaztahir.kte.parser

import com.wakaztahir.kte.model.model.MutableTemplateModel
import com.wakaztahir.kte.model.*
import com.wakaztahir.kte.parser.stream.*
import com.wakaztahir.kte.parser.stream.increment
import com.wakaztahir.kte.parser.stream.unexpected

//-------------- Reference

class ConstantReferenceParseException(message: String) : Throwable(message)

internal fun SourceStream.parseConstantReference(): ConstantReference? {
    if (currentChar == '@' && increment("@const(")) {
        val variableName = parseTextWhile { currentChar != ')' }
        increment(')')
        if (variableName.isNotEmpty()) {
            return ConstantReference(variableName)
        } else {
            throw ConstantReferenceParseException("constant reference must end with ')'")
        }
    }
    return null
}

//-------------- Declaration

internal data class ConstantDeclaration(val variableName: String, val variableValue: ReferencedValue) : AtDirective, DeclarationStatement {

    override fun storeValue(model: MutableTemplateModel) {
        model.putValue(variableName, variableValue)
    }

    override fun generateTo(block: LazyBlock, source: SourceStream, destination: DestinationStream) {

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
            val property = parseDynamicProperty()
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