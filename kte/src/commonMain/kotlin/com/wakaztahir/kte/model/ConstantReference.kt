package com.wakaztahir.kte.model

import com.wakaztahir.kte.TemplateContext

internal data class ConstantReference(val name: String) : ReferencedValue,AtDirective {
    override fun getValue(context: TemplateContext): DynamicValue<*>? {
        return context.getConstantReference(this)
    }
}