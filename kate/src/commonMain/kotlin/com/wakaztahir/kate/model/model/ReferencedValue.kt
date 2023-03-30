package com.wakaztahir.kate.model.model

import com.wakaztahir.kate.EmptyReferencedValuesList
import com.wakaztahir.kate.GetTypeModelReference
import com.wakaztahir.kate.model.*

interface ReferencedValue : KTEValue {

    override fun getModelReference(reference: ModelReference): KTEValue? {
        return null
    }

    fun toPlaceholderInvocation(model: MutableKTEObject, endPointer: Int): PlaceholderInvocation? {
        val value = getKTEValue(model)
        val typeFunction = (value.getModelReference(GetTypeModelReference)?.let { it as KTEFunction }) ?: return null
        return PlaceholderInvocation(
            placeholderName = (typeFunction.invoke(
                model = model,
                invokedOn = value,
                parameters = EmptyReferencedValuesList
            ) as StringValue).value,
            paramValue = value,
            invocationEndPointer = endPointer
        )
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