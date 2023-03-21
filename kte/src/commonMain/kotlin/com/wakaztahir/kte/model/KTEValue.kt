package com.wakaztahir.kte.model

import com.wakaztahir.kte.dsl.UnresolvedValueException
import com.wakaztahir.kte.model.model.ModelList
import com.wakaztahir.kte.model.model.TemplateModel

interface KTEValue {

    fun getNullablePrimitive(model: TemplateModel): PrimitiveValue<*>? {
        return null
    }

    fun getNullableIterable(model: TemplateModel): ModelList<KTEValue>? {
        return null
    }

    fun getNullableObject(model: TemplateModel): TemplateModel? {
        return null
    }

    private fun <T> throwValueException(model: TemplateModel, wanted: String): T {
        val actual = when {
            getNullablePrimitive(model) != null -> "Primitive"
            getNullableIterable(model) != null -> "Iterable"
            getNullableObject(model) != null -> "Object"
            else -> "None"
        }
        throw UnresolvedValueException("Required $actual is not $wanted")
    }

    fun getPrimitive(model: TemplateModel): PrimitiveValue<*> {
        return getNullablePrimitive(model) ?: throwValueException(model, "Value")
    }

    fun getIterable(model: TemplateModel): ModelList<KTEValue> {
        return getNullableIterable(model) ?: throwValueException(model, "Iterable")
    }

    fun getObject(model: TemplateModel): TemplateModel {
        return getNullableObject(model) ?: throwValueException(model, "Object")
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