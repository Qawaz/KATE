package com.wakaztahir.kte.model

import com.wakaztahir.kte.TemplateContext

internal class DynamicProperty(
    private val property: ReferencedValue?,
    private val value: DynamicValue<*>?,
) {

    init {
        require(property != null || value != null)
    }

    fun getReferencedProperty(): ReferencedValue? {
        return property
    }

    fun getStoredValue(): DynamicValue<*>? {
        return value
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