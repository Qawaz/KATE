package com.wakaztahir.kte.model

import com.wakaztahir.kte.model.model.KTEFunction
import com.wakaztahir.kte.model.model.KTEList
import com.wakaztahir.kte.model.model.KTEObject
import com.wakaztahir.kte.model.model.MutableKTEObject

interface ReferencedValue : KTEValue, CodeGen {

    fun asPrimitive(model: KTEObject): PrimitiveValue<*> {
        return this as PrimitiveValue<*>
    }

    fun asNullablePrimitive(model : KTEObject) : PrimitiveValue<*>? {
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