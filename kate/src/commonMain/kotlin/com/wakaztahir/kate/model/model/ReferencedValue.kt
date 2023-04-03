package com.wakaztahir.kate.model.model

import com.wakaztahir.kate.model.*

interface ReferencedValue : KATEValue {

    override fun getModelReference(reference: ModelReference): KATEValue? {
        return null
    }

    fun toPlaceholderInvocation(model: MutableKATEObject, endPointer: Int): PlaceholderInvocation? {
        val value = getKTEValue(model)
        val type = value.getKateType(model) ?: return null
        return PlaceholderInvocation(
            placeholderName = type,
            paramValue = value,
            invocationEndPointer = endPointer
        )
    }

    override fun asNullablePrimitive(model: KATEObject): PrimitiveValue<*>? {
        return getKTEValue(model) as? PrimitiveValue<*>
    }

    override fun asNullableList(model: KATEObject): KATEList<KATEValue>? {
        @Suppress("UNCHECKED_CAST")
        return getKTEValue(model) as? KATEList<KATEValue>
    }

    override fun asNullableObject(model: KATEObject): KATEObject? {
        return getKTEValue(model) as? KATEObject
    }

    override fun asNullableMutableObject(model: KATEObject): MutableKATEObject? {
        return getKTEValue(model) as? MutableKATEObject
    }

    override fun asNullableFunction(model: KATEObject): KATEFunction? {
        return getKTEValue(model) as? KATEFunction
    }

}