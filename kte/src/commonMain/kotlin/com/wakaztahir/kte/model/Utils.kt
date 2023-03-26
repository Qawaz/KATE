package com.wakaztahir.kte.model

import com.wakaztahir.kte.model.model.KTEObject
import com.wakaztahir.kte.model.model.KTEValue

fun KTEValue.asPrimitive(model: KTEObject): PrimitiveValue<*> {
    return asNullablePrimitive(model) ?: throw IllegalStateException("value is not a primitive")
}