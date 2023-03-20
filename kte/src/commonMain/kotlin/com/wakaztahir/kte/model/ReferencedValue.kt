package com.wakaztahir.kte.model

import com.wakaztahir.kte.dsl.ModelProvider

interface ReferencedValue {

    fun getValue(model: ModelProvider): DynamicValue<*>

}