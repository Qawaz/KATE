package com.wakaztahir.kte.model.model

import com.wakaztahir.kte.model.CodeGen
import com.wakaztahir.kte.model.PrimitiveValue

interface KTEValue : CodeGen {

    fun indentation(indentationLevel: Int): String {
        var indentation = ""
        repeat(indentationLevel) {
            indentation += '\t'
        }
        return indentation
    }

    fun stringValue(indentationLevel: Int): String

    override fun toString(): String

    fun asNullablePrimitive(model: KTEObject): PrimitiveValue<*>? {
        return this as? PrimitiveValue<*>
    }

    fun asNullableList(model: KTEObject): KTEList<KTEValue>? {
        @Suppress("UNCHECKED_CAST")
        return this as? KTEList<KTEValue>
    }

    fun asNullableObject(model: KTEObject): KTEObject? {
        return this as? KTEObject
    }

    fun asNullableMutableObject(model: KTEObject): MutableKTEObject? {
        return this as? MutableKTEObject
    }

    fun asNullableFunction(model: KTEObject): KTEFunction? {
        return this as? KTEFunction
    }


}