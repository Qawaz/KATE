package com.wakaztahir.kte.model

import com.wakaztahir.kte.TemplateContext
import com.wakaztahir.kte.dsl.ModelIterable
import com.wakaztahir.kte.model.model.TemplateModel

interface ReferencedValue : KTEValue {

    fun getValue(context: TemplateContext): DynamicValue<*> {
        return getValue(context.stream.model)
    }

    fun getValue(model: TemplateModel): DynamicValue<*>

    fun getIterable(model: TemplateModel): ModelIterable<KTEValue>?

}