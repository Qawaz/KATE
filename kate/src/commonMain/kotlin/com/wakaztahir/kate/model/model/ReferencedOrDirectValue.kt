package com.wakaztahir.kate.model.model

import com.wakaztahir.kate.model.KATEType
import com.wakaztahir.kate.model.PrimitiveValue

interface ReferencedOrDirectValue {

    fun getKATEValue(): KATEValue

    fun getKATEValueAndType() : Pair<KATEValue, KATEType?> {
        return Pair(getKATEValue(),null)
    }

    fun asNullablePrimitive(): PrimitiveValue<*>? {
        return getKATEValue() as? PrimitiveValue<*>
    }

    fun asNullableList(): KATEList<KATEValue>? {
        @Suppress("UNCHECKED_CAST")
        return getKATEValue() as? KATEList<KATEValue>
    }

    fun asNullableMutableList(): KATEMutableList<KATEValue>? {
        @Suppress("UNCHECKED_CAST")
        return getKATEValue() as? KATEMutableList<KATEValue>
    }

    fun asNullableObject(): KATEObject? {
        return getKATEValue() as? KATEObject
    }

    fun asNullableMutableObject(): MutableKATEObject? {
        return getKATEValue() as? MutableKATEObject
    }

    fun asNullableFunction(): KATEFunction? {
        return getKATEValue() as? KATEFunction
    }

}