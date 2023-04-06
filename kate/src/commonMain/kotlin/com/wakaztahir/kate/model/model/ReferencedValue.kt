package com.wakaztahir.kate.model.model

import com.wakaztahir.kate.model.*

interface ReferencedValue : KATEValue {

    fun toPlaceholderInvocation(model: MutableKATEObject, endPointer: Int): PlaceholderInvocation? {
        val value = getKTEValue(model)
        val type = value.getKateType(model) ?: return null
        return PlaceholderInvocation(
            placeholderName = type,
            paramValue = value,
            invocationEndPointer = endPointer
        )
    }

}