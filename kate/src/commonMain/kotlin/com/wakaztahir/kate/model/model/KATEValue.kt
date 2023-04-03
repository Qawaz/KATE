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
        return getKTEValue(model) as? PrimitiveValue<*>
    }

    fun asNullableList(model: KATEObject): KATEList<KATEValue>? {
        @Suppress("UNCHECKED_CAST")
        return getKTEValue(model) as? KATEList<KATEValue>
    }

    fun asNullableMutableList(model: KATEObject): KATEMutableList<KATEValue>? {
        @Suppress("UNCHECKED_CAST")
        return getKTEValue(model) as? KATEMutableList<KATEValue>
    }

    fun asNullableObject(model: KATEObject): KATEObject? {
        return getKTEValue(model) as? KATEObject
    }

    fun asNullableMutableObject(model: KATEObject): MutableKATEObject? {
        return getKTEValue(model) as? MutableKATEObject
    }

    fun asNullableFunction(model: KATEObject): KATEFunction? {
        return getKTEValue(model) as? KATEFunction
    }

    fun compareTo(model: KATEObject, other: KATEValue): Int

}