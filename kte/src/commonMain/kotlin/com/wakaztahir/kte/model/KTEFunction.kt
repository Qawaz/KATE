package com.wakaztahir.kte.model

import com.wakaztahir.kte.dsl.ModelValue
import com.wakaztahir.kte.model.model.ModelList
import com.wakaztahir.kte.model.model.TemplateModel

interface KTEFunction : KTEValue {

    fun invoke(model: TemplateModel, parameters: List<ReferencedValue>): ModelValue

    override fun getValue(model: TemplateModel): PrimitiveValue<*> {
        throw IllegalStateException("KTEFunction is not a primitive value")
    }

    override fun getObject(model: TemplateModel): TemplateModel {
        throw IllegalStateException("KTEFunction is not an object")
    }

    override fun getIterable(model: TemplateModel): ModelList<KTEValue> {
        throw IllegalStateException("KTEFunction is not an iterable")
    }

    override fun getFunction(model: TemplateModel): KTEFunction {
        return this
    }

    override fun stringValue(indentationLevel: Int): String {
        return toString()
    }

}