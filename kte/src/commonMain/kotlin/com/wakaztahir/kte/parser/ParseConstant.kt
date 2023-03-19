package com.wakaztahir.kte.parser

import com.wakaztahir.kte.TemplateContext
import com.wakaztahir.kte.model.AtDirective
import com.wakaztahir.kte.model.ConstantReference
import com.wakaztahir.kte.model.DynamicProperty
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

internal data class ConstantDeclaration(val variableName: String, val variableValue: DynamicProperty) : AtDirective {
    fun storeValue(context: TemplateContext) {
        context.storeValue(variableName, variableValue.getValue(context)!!.getValueAsString())
    }

    override fun generateTo(context: TemplateContext, stream: DestinationStream) {

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
            if (property != null) {
                return ConstantDeclaration(variableName = variableName, variableValue = property)
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