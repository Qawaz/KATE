package com.wakaztahir.kte.model

import com.wakaztahir.kte.model.model.KTEFunction
import com.wakaztahir.kte.model.model.KTEList
import com.wakaztahir.kte.model.model.KTEObject
import com.wakaztahir.kte.model.model.MutableKTEObject

interface ReferencedValue : KTEValue, CodeGen {

    fun getKTEValue(model: KTEObject): KTEValue {
        return this
    }

    fun asNullablePrimitive(model: KTEObject): PrimitiveValue<*>? {
        return getKTEValue(model) as? PrimitiveValue<*>
    }

    fun asNullableList(model: KTEObject): KTEList<KTEValue>? {
        @Suppress("UNCHECKED_CAST")
        return getKTEValue(model) as? KTEList<KTEValue>
    }

    fun asNullableObject(model: KTEObject): KTEObject? {
        return getKTEValue(model) as? KTEObject
    }

    fun asNullableMutableObject(model: KTEObject): MutableKTEObject? {
        return getKTEValue(model) as? MutableKTEObject
    }

    fun asNullableFunction(model: KTEObject): KTEFunction? {
        return getKTEValue(model) as? KTEFunction
    }

}