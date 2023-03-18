package com.wakaztahir.kte.parser

import com.wakaztahir.kte.TemplateContext
import com.wakaztahir.kte.parser.stream.*
import com.wakaztahir.kte.parser.stream.increment
import com.wakaztahir.kte.parser.stream.parseTextUntil
import com.wakaztahir.kte.parser.stream.unexpected

//-------------- Reference

class ConstantReferenceParseException(message: String) : Throwable(message)

internal fun SourceStream.parseConstantReference(): ConstantReference? {
    if (currentChar == '@' && increment("@const(")) {
        val variableName = parseTextUntil(')')
        if (variableName.isNotEmpty()) {
            return ConstantReference(variableName)
        } else {
            throw ConstantReferenceParseException("constant reference must end with ')'")
        }
    }
    return null
}

//-------------- Declaration

internal data class ConstantDeclaration(val variableName: String, val variableValue: DynamicProperty) {
    fun storeValue(context: TemplateContext) {
        variableValue.getValue(context)?.let { context.storeValue(variableName, it) }
    }
}

class ConstantDeclarationParseException(message: String) : Throwable(message)

internal fun TemplateContext.parseConstantDeclaration(): ConstantDeclaration? {
    if (stream.currentChar == '@' && stream.increment("@const ")) {
        val variableName = stream.parseTextUntil('=').trim()
        if (variableName.isNotEmpty()) {
            stream.increment("=")
            stream.escapeSpaces()
            val property = stream.parseDynamicProperty()
            if (property != null) {
                return ConstantDeclaration(variableName = variableName, variableValue = property)
            } else {
                throw ConstantDeclarationParseException("constant's value not found")
            }
        } else {
            if (stream.hasEnded) {
                stream.unexpected()
            } else {
                throw ConstantDeclarationParseException("constant's name not given")
            }
        }
    }
    return null
}