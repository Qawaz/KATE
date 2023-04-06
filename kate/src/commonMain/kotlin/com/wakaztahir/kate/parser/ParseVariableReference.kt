package com.wakaztahir.kate.parser

import com.wakaztahir.kate.model.ModelDirective
import com.wakaztahir.kate.model.ModelReference
import com.wakaztahir.kate.model.model.ReferencedValue
import com.wakaztahir.kate.parser.stream.*
import com.wakaztahir.kate.parser.stream.increment
import com.wakaztahir.kate.parser.stream.parseTextWhile

internal fun SourceStream.parseFunctionParameters(): List<ReferencedValue>? {
    if (increment('(')) {
        if (increment(')')) {
            return emptyList()
        }
        val parameters = mutableListOf<ReferencedValue>()
        do {
            val parameter = this.parseAnyExpressionOrValue(
                parseDirectRefs = true
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

private fun SourceStream.parseIndexingOperatorValue(parseDirectRefs: Boolean): ReferencedValue? {
    parseNumberValue()?.let { return it }
    parseVariableReference(parseDirectRefs = parseDirectRefs)?.let { return it }
    return null
}

internal fun SourceStream.parseIndexingOperatorCall(
    parseDirectRefs: Boolean,
    invokeOnly: Boolean
): ModelReference.FunctionCall? {
    if (increment('[')) {
        val indexingValue = parseIndexingOperatorValue(parseDirectRefs)
            ?: throw IllegalStateException("couldn't get indexing value inside indexing operator")
        if (increment(']')) {
            return ModelReference.FunctionCall(
                name = "get",
                invokeOnly = invokeOnly,
                parametersList = listOf(indexingValue)
            )
        } else {
            throw IllegalStateException("indexing operator must end with ']'")
        }
    }
    return null
}

internal fun SourceStream.parseDotReferencesInto(parseDirectRefs: Boolean): MutableList<ModelReference>? {
    var propertyPath: MutableList<ModelReference>? = null
    do {
        val invokeOnly = increment('@')
        if(currentChar.isDigit()){
            throw VariableReferenceParseException("variable name cannot begin with a digit")
        }
        val propertyName = parseTextWhile { currentChar.isVariableName() }
        val parameters = parseFunctionParameters()
        if (propertyPath == null) propertyPath = mutableListOf()
        if (parameters != null) {
            propertyPath.add(ModelReference.FunctionCall(propertyName, invokeOnly = invokeOnly, parameters))
        } else {
            propertyPath.add(ModelReference.Property(propertyName))
        }
        parseIndexingOperatorCall(parseDirectRefs, invokeOnly)?.let { propertyPath.add(it) }
    } while (increment('.'))
    return propertyPath
}

class VariableReferenceParseException(message: String) : Exception(message)

internal fun SourceStream.parseModelDirective(parseDirectRefs: Boolean): ModelDirective? {
    parseDotReferencesInto(parseDirectRefs = parseDirectRefs)?.let { return ModelDirective(it) }
    return null
}

internal fun SourceStream.parseVariableReference(parseDirectRefs: Boolean): ModelDirective? {
    if (currentChar == '@' && increment("@var(")) {
        val directive = parseModelDirective(parseDirectRefs = true)
        if (!increment(')')) {
            printErrorLineNumberAndCharacterIndex()
            throw VariableReferenceParseException("expected ) got $currentChar at $pointer")
        }
        return directive
    }
    if (parseDirectRefs) return parseModelDirective(true)?.let { return it }
    return null
}
