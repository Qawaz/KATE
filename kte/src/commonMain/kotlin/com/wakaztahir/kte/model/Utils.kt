package com.wakaztahir.kte.model

import com.wakaztahir.kte.model.model.KTEObject

fun ReferencedValue.asPrimitive(model: KTEObject): PrimitiveValue<*> {
    return asNullablePrimitive(model) ?: throw IllegalStateException("value is not a primitive")
}