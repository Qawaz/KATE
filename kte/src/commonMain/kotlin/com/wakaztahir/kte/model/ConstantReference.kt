package com.wakaztahir.kte.model

import com.wakaztahir.kte.TemplateContext
import com.wakaztahir.kte.parser.stream.DestinationStream

internal data class ConstantReference(val name: String) : ReferencedValue,AtDirective {
    override fun getValue(context: TemplateContext): DynamicValue<*>? {
        return context.getConstantReference(this)
    }

    override fun generateTo(context: TemplateContext, stream: DestinationStream) {
        getValue(context)!!.generateTo(context,stream)
    }
}