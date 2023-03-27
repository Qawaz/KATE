package com.wakaztahir.kte.model.model

import com.wakaztahir.kte.model.CodeGen
import com.wakaztahir.kte.model.ModelReference
import com.wakaztahir.kte.model.PrimitiveValue
import com.wakaztahir.kte.model.model.*

interface ReferencedValue : KTEValue, CodeGen {

    fun getKTEValue(model: KTEObject): KTEValue {
        return this
    }

    override fun getModelReference(reference: ModelReference): KTEValue? {
        return null
    }

    override fun asNullablePrimitive(model: KTEObject): PrimitiveValue<*>? {
        return getKTEValue(model) as? PrimitiveValue<*>
    }

    override fun asNullableList(model: KTEObject): KTEList<KTEValue>? {
        @Suppress("UNCHECKED_CAST")
        return getKTEValue(model) as? KTEList<KTEValue>
    }

    override fun asNullableObject(model: KTEObject): KTEObject? {
        return getKTEValue(model) as? KTEObject
    }

    override fun asNullableMutableObject(model: KTEObject): MutableKTEObject? {
        return getKTEValue(model) as? MutableKTEObject
    }

    override fun asNullableFunction(model: KTEObject): KTEFunction? {
        return getKTEValue(model) as? KTEFunction
    }

}