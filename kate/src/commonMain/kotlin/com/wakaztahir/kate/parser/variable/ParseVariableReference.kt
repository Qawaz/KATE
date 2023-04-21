package com.wakaztahir.kate.parser.variable

import com.wakaztahir.kate.model.ModelDirective
import com.wakaztahir.kate.model.ModelReference
import com.wakaztahir.kate.model.model.KATEValue
import com.wakaztahir.kate.parser.parseAnyExpressionOrValue
import com.wakaztahir.kate.parser.parseNumberValue
import com.wakaztahir.kate.parser.stream.*
import com.wakaztahir.kate.parser.stream.increment
import com.wakaztahir.kate.parser.stream.parseTextWhile

internal fun SourceStream.parseFunctionParameters(): List<KATEValue>? {
    if (increment('(')) {
        if (increment(')')) {
            return emptyList()
        }
        val parameters = mutableListOf<KATEValue>()
        do {
            val parameter = this.parseAnyExpressionOrValue(
                parseFirstStringOrChar = true,
                parseNotFirstStringOrChar = true,
                parseDirectRefs = true,
                allowAtLessExpressions = true
            )
            if (parameter != null) {
                parameters.add(parameter)
            } else {
                break
            }
        } while (increment(','))
        if (!increment(')')) {
            printErrorLineNumberAndCharacterIndex()
            throw IllegalStateException("a function call must end with ')' but instead found $currentChar")
        }
        return parameters
    }
    return null
}

private fun SourceStream.parseIndexingOperatorValue(parseDirectRefs: Boolean): KATEValue? {
    parseNumberValue()?.let { return it }
    parseVariableReference(parseDirectRefs = parseDirectRefs)?.let { return it }
    return null
}

internal fun SourceStream.parseIndexingOperatorCall(
    parseDirectRefs: Boolean,
): ModelReference.FunctionCall? {
    if (increment('[')) {
        val indexingValue = parseIndexingOperatorValue(parseDirectRefs)
            ?: throw IllegalStateException("couldn't get indexing value inside indexing operator")
        if (increment(']')) {
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

internal fun SourceStream.parseDotReferencesInto(
    parseDirectRefs: Boolean,
    throwOnEmptyVariableName: Boolean,
): MutableList<ModelReference>? {
    var propertyPath: MutableList<ModelReference>? = null
    val previous = pointer
    do {
        if (currentChar.isDigit()) {
            throw VariableReferenceParseException("variable name cannot begin with a digit")
        }
        val propertyName = parseTextWhile { currentChar.isVariableName() }
        if (propertyName.isEmpty()) {
            if (throwOnEmptyVariableName || propertyPath != null) {
                throw IllegalStateException("variable name cannot be empty")
            } else {
                setPointerAt(previous)
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
    } while (increment('.'))
    return propertyPath
}

class VariableReferenceParseException(message: String) : Exception(message)

internal fun SourceStream.parseModelDirective(
    parseDirectRefs: Boolean,
    throwOnEmptyVariableName: Boolean
): ModelDirective? {
    parseDotReferencesInto(
        parseDirectRefs = parseDirectRefs,
        throwOnEmptyVariableName = throwOnEmptyVariableName,
    )?.let { return ModelDirective(it) }
    return null
}

internal fun SourceStream.parseVariableReference(parseDirectRefs: Boolean): ModelDirective? {
    if (currentChar == '@' && increment("@var(")) {
        val directive = parseModelDirective(parseDirectRefs = true, throwOnEmptyVariableName = true)
        if (!increment(')')) {
            printErrorLineNumberAndCharacterIndex()
            throw VariableReferenceParseException("expected ')' got $currentChar at $pointer")
        }
        return directive
    }
    if (parseDirectRefs) return parseModelDirective(
        parseDirectRefs = true,
        throwOnEmptyVariableName = false
    )?.let { return it }
    return null
}
