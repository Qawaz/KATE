package com.wakaztahir.kate.parser

import com.wakaztahir.kate.model.model.MutableKATEObject
import com.wakaztahir.kate.model.*
import com.wakaztahir.kate.model.model.ReferencedValue
import com.wakaztahir.kate.parser.stream.*
import com.wakaztahir.kate.parser.stream.increment
import com.wakaztahir.kate.parser.stream.unexpected

internal data class VariableDeclaration(val variableName: String, val variableValue: ReferencedValue) : AtDirective {

    override val isEmptyWriter: Boolean
        get() = true

    fun storeValue(model: MutableKATEObject) {
        model.putValue(variableName, variableValue.getKTEValue(model))
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
            if(variableName.first().isDigit()){
                throw IllegalStateException("variable name cannot start with a digit $variableName")
            }
            if (isTakenVariableName(variableName)) {
                throw IllegalStateException("variable name cannot be $variableName")
            }
            source.escapeSpaces()
            source.increment('=')
            source.escapeSpaces()
            val property = source.parseAnyExpressionOrValue()
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