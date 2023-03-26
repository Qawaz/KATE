package com.wakaztahir.kte.parser

import com.wakaztahir.kte.model.*
import com.wakaztahir.kte.parser.stream.SourceStream
import com.wakaztahir.kte.parser.stream.increment
import com.wakaztahir.kte.parser.stream.parseTextWhile

internal fun SourceStream.parseFunctionParameters(): List<ReferencedValue>? {
    if (increment('(')) {
        if (increment(')')) {
            return emptyList()
        }
        val parameters = mutableListOf<ReferencedValue>()
        do {
            val parameter = this.parseAnyExpressionOrValue()
            if (parameter != null) {
                parameters.add(parameter)
            } else {
                break
            }
        } while (increment(','))
        if (!increment(')')) {
            throw IllegalStateException("a function call must end with ')'")
        }
        return parameters
    }
    return null
}

internal fun Char.isModelDirectiveLetter(): Boolean = this.isLetterOrDigit() || this == '_'

internal fun SourceStream.parseDotReferencesInto(propertyPath: MutableList<ModelReference>) {
    while (increment('.')) {
        var invokeOnly = false
        if (increment('@')) {
            if (increment('@')) {
                val propertyName = parseTextWhile { currentChar.isModelDirectiveLetter() }
                propertyPath.add(ModelReference.Property(propertyName))
                continue
            } else {
                invokeOnly = true
            }
        }
        val propertyName = parseTextWhile { currentChar.isModelDirectiveLetter() }
        val parameters = parseFunctionParameters()
        if (parameters != null) {
            propertyPath.add(ModelReference.FunctionCall(propertyName, invokeOnly = invokeOnly, parameters))
        } else {
            propertyPath.add(ModelReference.Property(propertyName))
        }
    }
}

internal fun SourceStream.parseModelDirective(directive: String = "@model"): ModelDirective? {
    if (currentChar == '@' && increment(directive)) {
        val propertyPath = mutableListOf<ModelReference>()
        parseDotReferencesInto(propertyPath)
        return ModelDirective(propertyPath)
    }
    return null
}