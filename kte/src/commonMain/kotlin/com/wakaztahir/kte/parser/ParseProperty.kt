package com.wakaztahir.kte.parser

import com.wakaztahir.kte.model.ReferencedValue
import com.wakaztahir.kte.parser.stream.SourceStream

internal fun SourceStream.parseReferencedValue(): ReferencedValue? {
    parseConstantReference()?.let { return it }
    parseModelDirective()?.let { return it }
    return null
}

internal fun SourceStream.parseDynamicProperty(): ReferencedValue? {
    parseConstantReference()?.let { return it }
    parseModelDirective()?.let { return it }
    parseDynamicValue()?.let { return it }
    return null
}