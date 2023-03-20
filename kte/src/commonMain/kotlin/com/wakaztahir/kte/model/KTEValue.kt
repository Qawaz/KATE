package com.wakaztahir.kte.model

import com.wakaztahir.kte.dsl.ModelIterable
import com.wakaztahir.kte.model.model.TemplateModel

interface KTEValue {

    fun getValue(model: TemplateModel): PrimitiveValue<*> {
        throw IllegalStateException("KTEValue Stub")
    }

    fun getIterable(model: TemplateModel): ModelIterable<KTEValue> {
        throw IllegalStateException("KTEValue Stub")
    }

    fun getObject(model: TemplateModel): TemplateModel {
        throw IllegalStateException("KTEValue Stub")
    }

    override fun toString() : String

}