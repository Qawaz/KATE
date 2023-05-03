package com.wakaztahir.kate.model.model

import com.wakaztahir.kate.model.KATEType
import com.wakaztahir.kate.model.ModelReference
import com.wakaztahir.kate.model.PlaceholderInvocation

interface ReferencedValue : ReferencedOrDirectValue {

    val propertyPath : List<ModelReference>

    val referenceModel : KATEObject

    fun toEmptyPlaceholderInvocation(model: MutableKATEObject, endPointer: Int): PlaceholderInvocation {
        model.getModelReferenceValue(path = propertyPath)
        return PlaceholderInvocation(
            placeholderName = KATEType.Unit.getKATEType(),
            definitionName = null,
            paramValue = KATEUnit,
            invocationEndPointer = endPointer
        )
    }

}