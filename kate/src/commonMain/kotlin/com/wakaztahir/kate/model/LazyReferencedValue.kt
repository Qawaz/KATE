package com.wakaztahir.kate.model

import com.wakaztahir.kate.model.model.KATEObject
import com.wakaztahir.kate.model.model.KATEValue
import com.wakaztahir.kate.model.model.ReferencedValue

class LazyReferencedValue(private val creator: () -> KATEValue) : ReferencedValue {

    private var _value: KATEValue? = null

    private val value: KATEValue
        get() {
            if (_value == null) _value = creator()
            return _value!!
        }

    override fun getKATEValue(model: KATEObject): KATEValue = value

    override fun getModelReference(reference: ModelReference): KATEValue? {
        return value.getModelReference(reference)
    }

    override fun toString(): String {
        return value.toString()
    }

    override fun compareTo(model: KATEObject, other: KATEValue): Int {
        return value.compareTo(model, other)
    }

}