package com.wakaztahir.kte.parser

import com.wakaztahir.kte.model.DynamicProperty
import com.wakaztahir.kte.parser.stream.SourceStream

internal fun SourceStream.parseDynamicProperty(): DynamicProperty? {
    parseConstantReference()?.let { return DynamicProperty(property = it, value = null) }
    parseDynamicValue()?.let { return DynamicProperty(property = null, value = it) }
    parseModelDirective()?.let { return DynamicProperty(property = it, value = null) }
    return null
}