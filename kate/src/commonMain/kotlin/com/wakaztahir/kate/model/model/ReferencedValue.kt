package com.wakaztahir.kate.model.model

import com.wakaztahir.kate.EmptyReferencedValuesList
import com.wakaztahir.kate.GetTypeModelReference
import com.wakaztahir.kate.model.*

interface ReferencedValue : KATEValue {

    override fun getKateType(model: KATEObject): String? {
        val typeFunction = (getModelReference(GetTypeModelReference)?.let { it as KATEFunction }) ?: return null
        return (typeFunction.invoke(
            model = model,
            invokedOn = this,
            parameters = EmptyReferencedValuesList
        ) as StringValue).value
    }

    fun toPlaceholderInvocation(model: MutableKATEObject, endPointer: Int): PlaceholderInvocation? {
        val value = getKATEValue(model)
        val type = value.getKateType(model) ?: return null
        return PlaceholderInvocation(
            placeholderName = type,
            definitionName = null,
            paramValue = value,
            invocationEndPointer = endPointer
        )
    }

}