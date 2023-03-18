package com.wakaztahir.kte.parser

import com.wakaztahir.kte.TemplateContext
import com.wakaztahir.kte.parser.stream.SourceStream
import com.wakaztahir.kte.parser.stream.increment
import com.wakaztahir.kte.parser.stream.parseTextUntil

internal interface DynamicProperty {
    fun getValue(context: TemplateContext): String?
}

internal class StringValue(private val value: String) : DynamicProperty {
    override fun getValue(context: TemplateContext): String {
        return value
    }
}

internal fun SourceStream.parseStringValue(): StringValue? {
    if (increment(until = { currentChar == '\"' && increment("\"") }, stopIf = { it != ' ' })) {
        val value = StringValue(parseTextUntil('\"'))
        increment("\"")
        return value
    }
    return null
}

data class ConstantReference(val variableName: String) : DynamicProperty {
    override fun getValue(context: TemplateContext): String? {
        return context.getPropertyValue(variableName)
    }
}

internal fun SourceStream.parseDynamicProperty(): DynamicProperty? {
    parseStringValue()?.let { return it }
    parseConstantReference()?.let { return it }
    return null
}