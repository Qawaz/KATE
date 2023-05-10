package com.wakaztahir.kate.parser.variable

import com.wakaztahir.kate.model.BooleanValue
import com.wakaztahir.kate.model.LazyBlock
import com.wakaztahir.kate.model.ModelDirective
import com.wakaztahir.kate.model.ModelReference
import com.wakaztahir.kate.model.model.KATEObject
import com.wakaztahir.kate.model.model.KATEValue
import com.wakaztahir.kate.model.model.ReferencedOrDirectValue
import com.wakaztahir.kate.model.model.ReferencedValue
import com.wakaztahir.kate.parser.*
import com.wakaztahir.kate.parser.parseAnyExpressionOrValue
import com.wakaztahir.kate.parser.parseStringValue
import com.wakaztahir.kate.parser.stream.*
import com.wakaztahir.kate.parser.stream.increment
import com.wakaztahir.kate.parser.stream.parseTextWhile

internal fun LazyBlock.parseFunctionParameters(): List<ReferencedOrDirectValue>? {
    if (source.increment('(')) {
        if (source.increment(')')) {
            return emptyList()
        }
        val parameters = mutableListOf<ReferencedOrDirectValue>()
        do {
            val parameter = parseAnyExpressionOrValue(
                parseDirectRefs = true
            )
            if (parameter != null) {
                parameters.add(parameter)
            } else {
                break
            }
        } while (source.increment(','))
        if (!source.increment(')')) {
            source.printErrorLineNumberAndCharacterIndex()
            throw IllegalStateException("a function call must end with ')' but instead found ${source.currentChar}")
        }
        return parameters
    }
    return null
}

private fun LazyBlock.parseIndexingOperatorValue(parseDirectRefs: Boolean): ReferencedOrDirectValue? {
    source.parseNumberValue()?.let { return it }
    source.parseStringValue()?.let { return it }
    parseVariableReference(parseDirectRefs = parseDirectRefs)?.let { return it }
    return null
}

internal fun LazyBlock.parseIndexingOperatorCall(
    parseDirectRefs: Boolean,
): ModelReference.FunctionCall? {
    if (source.increment('[')) {
        val indexingValue = parseIndexingOperatorValue(parseDirectRefs)
            ?: throw IllegalStateException("couldn't get indexing value inside indexing operator")
        if (source.increment(']')) {
            return ModelReference.FunctionCall(
                name = "get",
                parametersList = listOf(indexingValue)
            )
        } else {
            throw IllegalStateException("indexing operator must end with ']'")
        }
    }
    return null
}

internal fun LazyBlock.parseDotReferencesInto(
    parseDirectRefs: Boolean,
    throwOnEmptyVariableName: Boolean,
): MutableList<ModelReference>? {
    var propertyPath: MutableList<ModelReference>? = null
    val previous = source.pointer
    do {
        if (source.currentChar.isDigit()) {
            throw VariableReferenceParseException("variable name cannot begin with a digit")
        }
        val propertyName = source.parseTextWhile { currentChar.isVariableName() }
        if (propertyName.isEmpty()) {
            if (throwOnEmptyVariableName || propertyPath != null) {
                throw IllegalStateException("variable name cannot be empty")
            } else {
                source.setPointerAt(previous)
                return null
            }
        }
        val parameters = parseFunctionParameters()
        if (propertyPath == null) propertyPath = mutableListOf()
        if (parameters != null) {
            propertyPath.add(ModelReference.FunctionCall(propertyName, parameters))
        } else {
            propertyPath.add(ModelReference.Property(propertyName))
        }
        parseIndexingOperatorCall(parseDirectRefs = parseDirectRefs)?.let { propertyPath.add(it) }
    } while (source.increment('.'))
    return propertyPath
}

class VariableReferenceParseException(message: String) : Exception(message)

internal fun LazyBlock.parseModelDirective(
    parseDirectRefs: Boolean,
    throwOnEmptyVariableName: Boolean
): ModelDirective? {
    parseDotReferencesInto(
        parseDirectRefs = parseDirectRefs,
        throwOnEmptyVariableName = throwOnEmptyVariableName,
    )?.let { return ModelDirective(it, model) }
    return null
}

// returns a pair , where first value tells whether the expression is inside @var() or direct
internal fun LazyBlock.parseVariableReferenceAsExpression(parseDirectRefs: Boolean): Pair<Boolean,ReferencedOrDirectValue>? {
    if (source.currentChar == '@' && source.increment("@var(")) {
        val expression = parseAnyExpressionOrValue(parseDirectRefs = true)
        if (!source.increment(')')) {
            source.printErrorLineNumberAndCharacterIndex()
            throw VariableReferenceParseException("expected ')' got ${source.currentChar} at ${source.pointer}")
        }
        return expression?.let { Pair(true,it) }
    }
    if (parseDirectRefs) return parseModelDirective(
        parseDirectRefs = true,
        throwOnEmptyVariableName = false
    )?.let { return Pair(false,it) }
    return null
}

internal fun LazyBlock.parseVariableReference(parseDirectRefs: Boolean): ReferencedValue? {
    if (source.currentChar == '@' && source.increment("@var(")) {
        val directive = parseModelDirective(parseDirectRefs = true, throwOnEmptyVariableName = true)
        if (!source.increment(')')) {
            source.printErrorLineNumberAndCharacterIndex()
            throw VariableReferenceParseException("expected ')' got ${source.currentChar} at ${source.pointer}")
        }
        return directive
    }
    if (parseDirectRefs) return parseModelDirective(
        parseDirectRefs = true,
        throwOnEmptyVariableName = false
    )?.let { return it }
    return null
}
