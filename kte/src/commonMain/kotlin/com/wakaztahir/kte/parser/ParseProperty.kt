package com.wakaztahir.kte.parser

import com.wakaztahir.kte.model.DynamicProperty
import com.wakaztahir.kte.model.ReferencedValue
import com.wakaztahir.kte.parser.stream.SourceStream

internal fun SourceStream.parseReferencedValue(): ReferencedValue? {
    parseConstantReference()?.let { return it }
    parseModelDirective()?.let { return it }
    return null
}

internal fun SourceStream.parseDynamicProperty(): DynamicProperty? {
    parseConstantReference()?.let { return DynamicProperty(property = it, value = null) }
    parseModelDirective()?.let { return DynamicProperty(property = it, value = null) }
    parseDynamicValue()?.let { return DynamicProperty(property = null, value = it) }
    return null
}