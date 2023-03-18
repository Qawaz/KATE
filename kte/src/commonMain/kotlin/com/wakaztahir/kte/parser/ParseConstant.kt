package com.wakaztahir.kte.parser

import com.wakaztahir.kte.TemplateContext
import com.wakaztahir.kte.parser.stream.*
import com.wakaztahir.kte.parser.stream.increment
import com.wakaztahir.kte.parser.stream.parseTextUntil
import com.wakaztahir.kte.parser.stream.unexpected

//-------------- Reference

internal interface ConstantDeclarationValue {
    fun getValue(context: TemplateContext): String?
}

data class ConstantReference(val variableName: String) : ConstantDeclarationValue {
    override fun getValue(context: TemplateContext): String? {
        return context.getPropertyValue(variableName)
    }
}

class ConstantReferenceParseException(message: String) : Throwable(message)

fun TemplateContext.parseConstantReference(): ConstantReference? {
    if (stream.currentChar == '@' && stream.increment("@const(")) {
        val variableName = stream.parseTextUntil(')')
        if (variableName.isNotEmpty()) {
            return ConstantReference(variableName)
        } else {
            throw ConstantReferenceParseException("constant reference must end with ')'")
        }
    }
    return null
}

//-------------- Declaration

internal data class ConstantDeclaration(val variableName: String, val variableValue: ConstantDeclarationValue){
    fun storeValue(context: TemplateContext){
        variableValue.getValue(context)?.let { context.storeValue(variableName, it) }
    }
}

internal class StringValue(private val value: String) : ConstantDeclarationValue {
    override fun getValue(context: TemplateContext): String {
        return value
    }
}

class ConstantDeclarationParseException(message: String) : Throwable(message)

private fun SourceStream.parseStringValue(): StringValue? {
    if (incrementUntil(listOf('\'', '\"'), listOf(' '))) {
        if (currentChar == '\'') {
            increment("'")
            return StringValue(parseTextUntil('\''))
        } else if (currentChar == '\"') {
            increment("\"")
            return StringValue(parseTextUntil('\"'))
        }
    }
    return null
}

internal fun TemplateContext.parseConstantDeclaration(): ConstantDeclaration? {
    if (stream.currentChar == '@' && stream.increment("@const ")) {
        val variableName = stream.parseTextUntil('=').trim()
        if (variableName.isNotEmpty()) {
            stream.increment("=")

            var variableValue: ConstantDeclarationValue?

            variableValue = stream.parseStringValue()

            if (variableValue != null) {
                return ConstantDeclaration(variableName = variableName, variableValue = variableValue)
            }

            variableValue = parseConstantReference()
            if (variableValue != null) {
                return ConstantDeclaration(variableName = variableName, variableValue = variableValue)
            }

            throw ConstantDeclarationParseException("variable value not found")

        } else {
            if (stream.hasEnded) {
                stream.unexpected()
            } else {
                throw ConstantDeclarationParseException("variable name not given")
            }
        }
    }
    return null
}