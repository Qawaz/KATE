package com.wakaztahir.kte.model

import com.wakaztahir.kte.dsl.ModelProvider

class DynamicProperty(
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

    fun getValue(model: ModelProvider): DynamicValue<*> {
        return value ?: property!!.getValue(model)
    }

}