package com.wakaztahir.kte.parser

import com.wakaztahir.kte.model.*
import com.wakaztahir.kte.model.model.KTEValue
import com.wakaztahir.kte.parser.stream.SourceStream
import com.wakaztahir.kte.parser.stream.increment
import com.wakaztahir.kte.parser.stream.parseTextWhile

internal fun LazyBlock.parseFunctionParameters(): List<KTEValue>? {
    if (source.increment('(')) {
        if (source.increment(')')) {
            return emptyList()
        }
        val parameters = mutableListOf<KTEValue>()
        do {
            val parameter = parseAnyExpressionOrValue()
            if (parameter != null) {
                parameters.add(parameter)
            } else {
                break
            }
        } while (source.increment(','))
        if (!source.increment(')')) {
            throw IllegalStateException("a function call must end with ')'")
        }
        return parameters
    }
    return null
}

internal fun Char.isModelDirectiveLetter(): Boolean = this.isLetterOrDigit() || this == '_'

internal fun LazyBlock.parseDotReferencesInto(propertyPath: MutableList<ModelReference>) {
    while (source.increment('.')) {
        var invokeOnly = false
        if (source.increment('@')) {
            if (source.increment('@')) {
                val propertyName = source.parseTextWhile { currentChar.isModelDirectiveLetter() }
                propertyPath.add(ModelReference.Property(propertyName))
                continue
            } else {
                invokeOnly = true
            }
        }
        val propertyName = source.parseTextWhile { currentChar.isModelDirectiveLetter() }
        val parameters = parseFunctionParameters()
        if (parameters != null) {
            propertyPath.add(ModelReference.FunctionCall(propertyName, invokeOnly = invokeOnly, parameters))
        } else {
            propertyPath.add(ModelReference.Property(propertyName))
        }
    }
}

internal fun LazyBlock.parseModelDirective(directive: String = "@model"): ModelDirective? {
    if (source.currentChar == '@' && source.increment(directive)) {
        val propertyPath = mutableListOf<ModelReference>()
        parseDotReferencesInto(propertyPath)
        return ModelDirective(propertyPath)
    }
    return null
}