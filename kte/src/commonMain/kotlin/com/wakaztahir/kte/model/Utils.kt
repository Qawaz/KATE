package com.wakaztahir.kte.model

import com.wakaztahir.kte.model.model.KTEObject
import com.wakaztahir.kte.model.model.ReferencedValue

fun ReferencedValue.asPrimitive(model: KTEObject): PrimitiveValue<*> {
    return asNullablePrimitive(model) ?: throw IllegalStateException("value is not a primitive")
}

fun indentation(indentationLevel: Int): String {
    var indentation = ""
    repeat(indentationLevel) {
        indentation += '\t'
    }
    return indentation
}