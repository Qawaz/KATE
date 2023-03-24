package com.wakaztahir.kte.model

import com.wakaztahir.kte.model.model.KTEList
import com.wakaztahir.kte.model.model.KTEObject

interface ReferencedValue : KTEValue, CodeGen {

    fun asPrimitive(model: KTEObject): PrimitiveValue<*> {
        return this as PrimitiveValue<*>
    }

    fun asList(model: KTEObject): KTEList<KTEValue> {
        @Suppress("UNCHECKED_CAST")
        return this as KTEList<KTEValue>
    }

    fun asObject(model: KTEObject): KTEObject {
        return this as KTEObject
    }

    fun asFunction(model: KTEObject): KTEFunction {
        return this as KTEFunction
    }

}