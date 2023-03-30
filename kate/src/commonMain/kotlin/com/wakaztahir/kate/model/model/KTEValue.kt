package com.wakaztahir.kate.model.model

import com.wakaztahir.kate.EmptyReferencedValuesList
import com.wakaztahir.kate.GetTypeModelReference
import com.wakaztahir.kate.model.ModelReference
import com.wakaztahir.kate.model.PlaceholderInvocation
import com.wakaztahir.kate.model.PrimitiveValue
import com.wakaztahir.kate.model.StringValue

interface KTEValue {

    fun getKTEValue(model: KTEObject): KTEValue {
        return this
    }

    fun getModelReference(reference: ModelReference): KTEValue?

    fun getKateType(model: KTEObject): String? {
        val typeFunction = (getModelReference(GetTypeModelReference)?.let { it as KTEFunction }) ?: return null
        return (typeFunction.invoke(
            model = model,
            invokedOn = this,
            parameters = EmptyReferencedValuesList
        ) as StringValue).value
    }

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

    fun compareTo(model: KTEObject, other: KTEValue): Int

}