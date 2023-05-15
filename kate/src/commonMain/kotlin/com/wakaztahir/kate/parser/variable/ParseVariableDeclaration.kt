package com.wakaztahir.kate.parser.variable

import com.wakaztahir.kate.model.model.MutableKATEObject
import com.wakaztahir.kate.model.*
import com.wakaztahir.kate.model.model.KATEObject
import com.wakaztahir.kate.model.model.KATEValue
import com.wakaztahir.kate.model.model.ReferencedOrDirectValue
import com.wakaztahir.kate.parser.parsePrimitiveValue
import com.wakaztahir.kate.parser.stream.*
import com.wakaztahir.kate.parser.stream.increment
import com.wakaztahir.kate.tokenizer.NodeTokenizer

class VariableDeclarationException(message: String) : Exception(message)

data class VariableDeclaration(
    val variableName: String,
    val type: KATEType?,
    val variableValue: ReferencedOrDirectValue,
    val provider: ModelProvider
) : AtDirective {

    override fun <T> selectNode(tokenizer: NodeTokenizer<T>): T = tokenizer.variableDeclaration

    override val isEmptyWriter: Boolean
        get() = true

    fun storeValue(model : MutableKATEObject = provider.model) {
        val value = variableValue.getKATEValueAndType()
        val actualType = value.second ?: value.first.getKnownKATEType()
        if (type != null) {
            if (!type.satisfiedBy(actualType)) {
                throw IllegalStateException("cannot satisfy type $type with $actualType")
            }
            if (type != actualType) {
                model.setExplicitType(variableName, type)
            }
        } else {
            if (value.second != null) model.setExplicitType(variableName, value.second!!)
        }
        if (!model.insertValue(variableName, value.first)) {
            throw VariableDeclarationException("couldn't declare variable $variableName which already exists in model $model")
        }
    }

    override fun generateTo(destination: DestinationStream) {
        storeValue()
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

private fun SourceStream.parseMetaValues(): MutableMap<String, KATEValue>? {
    if (increment('`')) {
        val meta = mutableMapOf<String, KATEValue>()
        do {
            val metaName = parseTextWhile { currentChar.isVariableName() }
            escapeSpaces()
            if (increment('=')) {
                escapeSpaces()
                val metaValue = parsePrimitiveValue()
                    ?: parseTextWhile { currentChar.isVariableName() }.ifEmpty { null }?.let { StringValue(it) }
                    ?: throw IllegalStateException("expected a value for meta property $metaName got $currentChar")
                meta[metaName] = metaValue
            } else {
                throw IllegalStateException("expected '=' when declaring a class property got $currentChar")
            }
        } while (increment(','))
        if (!increment('`')) {
            throw IllegalStateException("meta properties beginning with '`' must end with '`'")
        } else {
            escapeSpaces()
        }
        return meta
    }
    return null
}

private fun SourceStream.parseClassProperty(): KATEType {
    if (increment(':')) {
        escapeSpaces()
        return parseKATEType(parseMetadata = true)
            ?: throw IllegalStateException("expected a type after ':' got $currentChar")
    } else {
        throw IllegalStateException("expected ':' after variable name in class type got $currentChar")
    }
}

private fun SourceStream.parseClassType(): KATEType.Class? {
    val previous = pointer
    if (increment('{')) {
        val members = mutableMapOf<String, KATEType>()
        do {
            escapeSpaces()
            if (increment('}')) break
            val variableName = parseTextWhile { currentChar.isVariableName() }
            escapeSpaces()
            members[variableName] = parseClassProperty()
        } while (increment(';'))
        return KATEType.Class(members)
    }
    setPointerAt(previous)
    return null
}

internal fun SourceStream.parseKATEType(parseMetadata: Boolean): KATEType? {
    val previous = pointer
    parseClassType()?.let { return it }
    val type = parseTextWhile { currentChar.isTypeName() }
    if (type.isNotEmpty()) {
        var typeName = when (type) {
            "any" -> KATEType.Any
            "char" -> KATEType.Char
            "string" -> KATEType.String
            "int" -> KATEType.Int
            "double" -> KATEType.Double
            "long" -> KATEType.Long
            "boolean" -> KATEType.Boolean
            "list", "mutable_list" -> {
                increment('<')
                val itemType = parseKATEType(parseMetadata = false) ?: KATEType.NullableKateType(KATEType.Any)
                increment('>')
                if (type == "list") KATEType.List(itemType) else KATEType.MutableList(itemType)
            }

            "object" -> {
                increment('<')
                val itemType = parseKATEType(parseMetadata = false) ?: KATEType.NullableKateType(KATEType.Any)
                increment('>')
                KATEType.Object(itemType)
            }

            else -> {
                throw IllegalStateException("unknown type name $type")
            }
        }
        if (increment('?')) typeName = KATEType.NullableKateType(actual = typeName)

        if (parseMetadata) {
            escapeSpaces()
            val meta = parseMetaValues()
            if (meta != null) typeName = KATEType.TypeWithMetadata(actual = typeName,meta = meta)
        }

        return typeName
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
                source.parseKATEType(parseMetadata = false)
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
                    type = type,
                    provider = provider
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