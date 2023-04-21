package com.wakaztahir.kate.model.model

import com.wakaztahir.kate.model.KATEType
import com.wakaztahir.kate.model.ModelReference
import com.wakaztahir.kate.model.PlaceholderInvocation
import com.wakaztahir.kate.model.PrimitiveValue

interface KATEValue {

    fun getKATEValue(model: KATEObject): KATEValue {
        return this
    }

    @Deprecated("use getKATEValue", replaceWith = ReplaceWith(expression = "getKATEValue(model)"))
    fun getKTEValue(model: KATEObject): KATEValue {
        return getKATEValue(model)
    }

    fun getModelReference(reference: ModelReference): KATEValue?

    fun getKnownKATEType(): KATEType?

    fun getKATEType(model: KATEObject): KATEType

    override fun toString(): String

    fun toPlaceholderInvocation(model: MutableKATEObject, endPointer: Int): PlaceholderInvocation? {
        val value = getKATEValue(model)
        val type = value.getKATEType(model)
        return PlaceholderInvocation(
            placeholderName = type.getPlaceholderName(),
            definitionName = null,
            paramValue = value,
            invocationEndPointer = endPointer
        )
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

    fun compareTo(model: KATEObject, other: KATEValue): Int

}