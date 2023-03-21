package com.wakaztahir.kte.model

import com.wakaztahir.kte.model.model.ModelList
import com.wakaztahir.kte.model.model.TemplateModel

interface KTEValue {

    fun getValue(model: TemplateModel): PrimitiveValue<*> {
        throw IllegalStateException("KTEValue Stub")
    }

    fun getIterable(model: TemplateModel): ModelList<KTEValue> {
        throw IllegalStateException("KTEValue Stub")
    }

    fun getObject(model: TemplateModel): TemplateModel {
        throw IllegalStateException("KTEValue Stub")
    }

    fun indentation(indentationLevel: Int): String {
        var indentation = ""
        repeat(indentationLevel) {
            indentation += '\t'
        }
        return indentation
    }

    fun stringValue(indentationLevel: Int): String

    override fun toString(): String

}