package com.wakaztahir.kate.parser.variable

import com.wakaztahir.kate.model.model.MutableKATEObject
import com.wakaztahir.kate.model.*
import com.wakaztahir.kate.model.model.ReferencedOrDirectValue
import com.wakaztahir.kate.parser.stream.*
import com.wakaztahir.kate.parser.stream.increment

class VariableDeclarationException(message: String) : Exception(message)

internal data class VariableDeclaration(
    val variableName: String,
    val type: KATEType?,
    val variableValue: ReferencedOrDirectValue
) : AtDirective {

    override val isEmptyWriter: Boolean
        get() = true

    fun storeValue(model: MutableKATEObject) {
        val value = variableValue.getKATEValue(model)
        val actualType = value.getKnownKATEType()
        if (type != null) {
            if (!actualType.satisfies(type)) {
                throw IllegalStateException("cannot satisfy type $type with $actualType")
            }
            if (type != actualType) {
                model.setVariableExplicitType(variableName, type)
            }
        }
        if (!model.insertValue(variableName, value)) {
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

private fun Char.isTypeName(): Boolean = this.isLetter() || this == '_'

internal fun SourceStream.parseKATEType(): KATEType? {
    val previous = pointer
    val type = parseTextWhile { currentChar.isTypeName() }
    if (type.isNotEmpty()) {
        val typeName = when (type) {
            "any" -> KATEType.Any
            "char" -> KATEType.Char
            "string" -> KATEType.String
            "int" -> KATEType.Int
            "double" -> KATEType.Double
            "long" -> KATEType.Long
            "boolean" -> KATEType.Boolean
            "list", "mutable_list" -> {
                source.increment('<')
                val itemType = parseKATEType() ?: KATEType.NullableKateType(KATEType.Any)
                source.increment('>')
                if (type == "list") KATEType.List(itemType) else KATEType.MutableList(itemType)
            }

            "object" -> {
                source.increment('<')
                val itemType = parseKATEType() ?: KATEType.NullableKateType(KATEType.Any)
                source.increment('>')
                KATEType.Object(itemType)
            }

            else -> {
                throw IllegalStateException("unknown type name $type")
            }
        }
        val isNullable = increment('?')
        typeName.let { return if (isNullable) KATEType.NullableKateType(it) else it }
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
                source.parseKATEType()
            } else {
                null
            }
            source.escapeSpaces()
            if (!source.increment('=')) {
                throw IllegalStateException("expected '=' when assigning a value to variable \"$variableName\" but got '${source.currentChar}' in variable declaration")
            }
            source.escapeSpaces()
            val property = parseValueOfType(
                type = type ?: KATEType.Any,
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