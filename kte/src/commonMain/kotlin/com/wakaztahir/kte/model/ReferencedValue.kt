package com.wakaztahir.kte.model

import com.wakaztahir.kte.TemplateContext

internal interface ReferencedValue {

    fun getValue(context: TemplateContext): DynamicValue<*>?

}