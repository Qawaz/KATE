package com.wakaztahir.kate.model

import com.wakaztahir.kate.model.model.KATEObject
import com.wakaztahir.kate.model.model.ReferencedValue

fun ReferencedValue.asPrimitive(model: KATEObject): PrimitiveValue<*> {
    return asNullablePrimitive(model) ?: throw IllegalStateException("value is not a primitive")
}

