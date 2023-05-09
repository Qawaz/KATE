package com.wakaztahir.kate.model.model

import com.wakaztahir.kate.model.KATEType
import com.wakaztahir.kate.model.PlaceholderInvocation
import com.wakaztahir.kate.model.PrimitiveValue

interface ReferencedOrDirectValue {

    fun getKATEValue(model: KATEObject): KATEValue

    fun getKATEValueAndType(model : KATEObject) : Pair<KATEValue, KATEType?> {
        return Pair(getKATEValue(model),null)
    }

    fun asNullablePrimitive(model: KATEObject): PrimitiveValue<*>? {
        return getKATEValue(model) as? PrimitiveValue<*>
    }

    fun asNullableList(model: KATEObject): KATEList<KATEValue>? {
        @Suppress("UNCHECKED_CAST")
        return getKATEValue(model) as? KATEList<KATEValue>
    }

    fun asNullableMutableList(model: KATEObject): KATEMutableList<KATEValue>? {
        @Suppress("UNCHECKED_CAST")
        return getKATEValue(model) as? KATEMutableList<KATEValue>
    }

    fun asNullableObject(model: KATEObject): KATEObject? {
        return getKATEValue(model) as? KATEObject
    }

    fun asNullableMutableObject(model: KATEObject): MutableKATEObject? {
        return getKATEValue(model) as? MutableKATEObject
    }

    fun asNullableFunction(model: KATEObject): KATEFunction? {
        return getKATEValue(model) as? KATEFunction
    }

}