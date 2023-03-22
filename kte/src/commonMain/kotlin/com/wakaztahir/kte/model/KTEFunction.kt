package com.wakaztahir.kte.model

import com.wakaztahir.kte.dsl.ModelValue
import com.wakaztahir.kte.model.model.KTEList
import com.wakaztahir.kte.model.model.KTEObject

interface KTEFunction : KTEValue {

    fun invoke(model: KTEObject, parameters: List<ReferencedValue>): ModelValue

    override fun asPrimitive(model: KTEObject): PrimitiveValue<*> {
        throw IllegalStateException("KTEFunction is not a primitive value")
    }

    override fun asObject(model: KTEObject): KTEObject {
        throw IllegalStateException("KTEFunction is not an object")
    }

    override fun asList(model: KTEObject): KTEList<KTEValue> {
        throw IllegalStateException("KTEFunction is not an iterable")
    }

    override fun asFunction(model: KTEObject): KTEFunction {
        return this
    }

    override fun stringValue(indentationLevel: Int): String {
        return toString()
    }

}