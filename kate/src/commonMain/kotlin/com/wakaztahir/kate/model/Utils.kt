package com.wakaztahir.kate.model

import com.wakaztahir.kate.model.model.ReferencedOrDirectValue

fun ReferencedOrDirectValue.asPrimitive(): PrimitiveValue<*> {
    return asNullablePrimitive() ?: throw IllegalStateException("value is not a primitive")
}

