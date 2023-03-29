package com.wakaztahir.kte.model.model

import com.wakaztahir.kte.model.CodeGen
import com.wakaztahir.kte.model.ModelReference
import com.wakaztahir.kte.model.PrimitiveValue

interface KTEValue {

    fun getModelReference(reference: ModelReference): KTEValue?

    fun stringValue(indentationLevel: Int): String

    override fun toString(): String

    fun asNullablePrimitive(model: KTEObject): PrimitiveValue<*>? {
        return this as? PrimitiveValue<*>
    }

    fun asNullableList(model: KTEObject): KTEList<KTEValue>? {
        @Suppress("UNCHECKED_CAST")
        return this as? KTEList<KTEValue>
    }

    fun asNullableMutableList(model: KTEObject): KTEMutableList<KTEValue>? {
        @Suppress("UNCHECKED_CAST")
        return this as? KTEMutableList<KTEValue>
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

    fun compareTo(model: KTEObject,other: KTEValue): Int

}