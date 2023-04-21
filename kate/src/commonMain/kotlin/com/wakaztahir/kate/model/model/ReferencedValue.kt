package com.wakaztahir.kate.model.model

import com.wakaztahir.kate.model.*

interface ReferencedValue : KATEValue {

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

}