package com.wakaztahir.kate.model.model

import com.wakaztahir.kate.EmptyReferencedValuesList
import com.wakaztahir.kate.GetTypeModelReference
import com.wakaztahir.kate.model.ModelReference
import com.wakaztahir.kate.model.PrimitiveValue
import com.wakaztahir.kate.model.StringValue

interface KATEValue {

    fun getKTEValue(model: KATEObject): KATEValue {
        return this
    }

    fun getModelReference(reference: ModelReference): KATEValue?

    fun getKateType(model: KATEObject): String? {
        val typeFunction = (getModelReference(GetTypeModelReference)?.let { it as KATEFunction }) ?: return null
        return (typeFunction.invoke(
            model = model,
            invokedOn = this,
            parameters = EmptyReferencedValuesList
        ) as StringValue).value
    }

    override fun toString(): String

    fun asNullablePrimitive(model: KATEObject): PrimitiveValue<*>? {
        return this as? PrimitiveValue<*>
    }

    fun asNullableList(model: KATEObject): KATEList<KATEValue>? {
        @Suppress("UNCHECKED_CAST")
        return this as? KATEList<KATEValue>
    }

    fun asNullableMutableList(model: KATEObject): KATEMutableList<KATEValue>? {
        @Suppress("UNCHECKED_CAST")
        return this as? KATEMutableList<KATEValue>
    }

    fun asNullableObject(model: KATEObject): KATEObject? {
        return this as? KATEObject
    }

    fun asNullableMutableObject(model: KATEObject): MutableKATEObject? {
        return this as? MutableKATEObject
    }

    fun asNullableFunction(model: KATEObject): KATEFunction? {
        return this as? KATEFunction
    }

    fun compareTo(model: KATEObject, other: KATEValue): Int

}