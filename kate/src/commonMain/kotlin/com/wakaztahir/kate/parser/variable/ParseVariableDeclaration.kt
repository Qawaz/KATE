package com.wakaztahir.kate.parser.variable

import com.wakaztahir.kate.model.model.MutableKATEObject
import com.wakaztahir.kate.model.*
import com.wakaztahir.kate.model.model.KATEValue
import com.wakaztahir.kate.model.model.ReferencedValue
import com.wakaztahir.kate.parser.parseAnyExpressionOrValue
import com.wakaztahir.kate.parser.stream.*
import com.wakaztahir.kate.parser.stream.increment

class VariableDeclarationException(message: String) : Exception(message)

internal data class VariableDeclaration(
    val variableName: String,
    val type: KATEType?,
    val variableValue: ReferencedValue
) : AtDirective {

    override val isEmptyWriter: Boolean
        get() = true

    fun storeValue(model: MutableKATEObject) {
        if (type != null) {
            model.setExplicitType(variableName, type)
        }
        if (!model.insertValue(variableName, variableValue.getKATEValue(model))) {
            throw VariableDeclarationException("couldn't declare variable $variableName which already exists")
        }
    }

    override fun generateTo(block: LazyBlock, destination: DestinationStream) {
        storeValue(block.model)
    }

}

internal fun SourceStream.parseVariableName(): String? {
    val previous = pointer
    if (currentChar == '@' && increment("@var")) {
        if (currentChar == '(') {
            setPointerAt(previous)
            return null
        }
        increment(' ')
        return parseTextWhile { currentChar.isVariableName() }
    }
    return null
}

private fun Char.isTypeName(): Boolean = this.isLetter() || this == '<' || this == '>'

internal fun SourceStream.parseVariableDeclarationType(): KATEType? {
    val previous = pointer
    val type = parseTextWhile { currentChar.isTypeName() }
    if (type.isNotEmpty()) {
        val isNullable = increment('?')
        val typeName = when (type) {
            "char" -> KATEType.Char()
            "string" -> KATEType.String()
            "int" -> KATEType.Int()
            "double" -> KATEType.Double()
            "long" -> KATEType.Long()
            "boolean" -> KATEType.Boolean()
            else -> null
        }
        typeName?.let { return if (isNullable) KATEType.NullableKateType(it) else it }
    }
    setPointerAt(previous)
    return null
}

internal fun LazyBlock.parseVariableDeclaration(): VariableDeclaration? {
    val variableName = source.parseVariableName()
    if (variableName != null) {
        if (variableName.isNotEmpty()) {
            val valid = isValidVariableName(variableName)
            if (valid.isFailure) throw valid.exceptionOrNull()!!
            source.escapeSpaces()
            val type: KATEType? = if (source.increment(':')) {
                source.escapeSpaces()
                source.parseVariableDeclarationType()
            } else {
                null
            }
            source.escapeSpaces()
            if (!source.increment('=')) {
                throw IllegalStateException("expected '=' when assigning a value to variable \"$variableName\" but got '${source.currentChar}' in variable declaration")
            }
            source.escapeSpaces()
            val property = parseValueOfType(
                type = type ?: KATEType.Any(),
                allowAtLessExpressions = true,
                parseDirectRefs = true
            )
            return if (property != null) {
                VariableDeclaration(
                    variableName = variableName,
                    variableValue = property,
                    type = type
                )
            } else {
                throw VariableDeclarationException("constant's value not found when declaring variable $variableName")
            }
        } else {
            if (source.hasEnded) {
                throw UnexpectedEndOfStream("unexpected end of stream at pointer : ${source.pointer}")
            } else {
                source.printLeft()
                throw VariableDeclarationException("variable's name not given or is empty")
            }
        }
    }
    return null
}