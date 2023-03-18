package com.wakaztahir.kte.parser

import com.wakaztahir.kte.TemplateContext
import com.wakaztahir.kte.model.DynamicValue
import com.wakaztahir.kte.model.StringValue
import com.wakaztahir.kte.parser.stream.SourceStream
import com.wakaztahir.kte.parser.stream.TextStream
import com.wakaztahir.kte.parser.stream.printLeft

internal interface DynamicProperty {

    val name: String

    fun getValue(context: TemplateContext): DynamicValue<*>? {
        return context.getPropertyValue(name)?.let { TextStream(it).parseDynamicValue() }
    }

    @Deprecated("don't use")
    fun getValueAsString(context: TemplateContext): String? {
        return (getValue(context) as StringValue).value
    }


}

internal class PropertyOrValue(private val property: DynamicProperty?, private val value: DynamicValue<*>?) {
    init {
        require(property != null || value != null)
    }

    fun getValue(context: TemplateContext): DynamicValue<*>? {
        if (property != null) {
            return property.getValue(context)
        }
        if (value != null) {
            return value
        }
        return null!!
    }
}

internal data class ConstantReference(override val name: String) : DynamicProperty {

}

internal fun SourceStream.parseDynamicProperty(): PropertyOrValue? {
    parseConstantReference()?.let { return PropertyOrValue(property = it, value = null) }
    parseDynamicValue()?.let { return PropertyOrValue(property = null, value = it) }
    return null
}