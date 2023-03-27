package com.wakaztahir.kte.parser

import com.wakaztahir.kte.model.model.MutableKTEObject
import com.wakaztahir.kte.model.*
import com.wakaztahir.kte.model.model.ReferencedValue
import com.wakaztahir.kte.parser.stream.*
import com.wakaztahir.kte.parser.stream.increment
import com.wakaztahir.kte.parser.stream.unexpected

internal data class VariableDeclaration(val variableName: String, val variableValue: ReferencedValue) : AtDirective {

    override val isEmptyWriter: Boolean
        get() = true

    fun storeValue(model: MutableKTEObject) {
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
        else -> false
    }
}

internal fun LazyBlock.parseVariableDeclaration(): VariableDeclaration? {
    val variableName = source.parseVariableName()
    if (variableName != null) {
        if (variableName.isNotEmpty()) {
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

private class DeleteVar(val propertyPath: List<ModelReference>) : CodeGen {
    override fun generateTo(block: LazyBlock, destination: DestinationStream) {
        block.model.remove(model = block.model, path = propertyPath)
    }
}

fun LazyBlock.parseDeleteVarDirective(): CodeGen? {
    if (source.currentChar == '@' && source.increment("delete_var(")) {
        val propertyPath = mutableListOf<ModelReference>()
        source.parseDotReferencesInto(propertyPath)
        if (!source.increment(')')) {
            throw VariableReferenceParseException("expected ) got ${source.currentChar} at ${source.pointer}")
        }
        return DeleteVar(propertyPath)
    }
    return null
}