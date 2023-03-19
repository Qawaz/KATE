package com.wakaztahir.kte.parser

import com.wakaztahir.kte.model.*
import com.wakaztahir.kte.parser.stream.SourceStream
import com.wakaztahir.kte.parser.stream.increment
import com.wakaztahir.kte.parser.stream.parseTextWhile

internal fun SourceStream.parseFunctionParameters(): List<DynamicProperty>? {
    if (increment('(')) {
        if (increment(')')) {
            return emptyList()
        }
        val parameters = mutableListOf<DynamicProperty>()
        do {
            val parameter = parseDynamicProperty()
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

internal fun SourceStream.parseModelDirective(): ModelDirective? {
    if (currentChar == '@' && increment("@model")) {
        val propertyPath = mutableListOf<ModelReference>()
        while (increment('.')) {
            if (increment('@')) {
                val functionName = parseTextWhile { currentChar != '(' }
                val parametersList =
                    parseFunctionParameters() ?: throw IllegalStateException("function parameters not found")
                propertyPath.add(ModelReference.FunctionCall(functionName, parametersList))
            } else {
                val propertyName = parseTextWhile { currentChar.isModelDirectiveLetter() }
                propertyPath.add(ModelReference.Property(propertyName))
            }
        }
        return ModelDirective(propertyPath)
    }
    return null
}