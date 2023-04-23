package com.wakaztahir.kate.model

import com.wakaztahir.kate.model.model.KATEObject
import com.wakaztahir.kate.model.model.KATEValue
import com.wakaztahir.kate.model.model.ReferencedOrDirectValue

class LazyReferencedValue(private val creator: () -> ReferencedOrDirectValue) : ReferencedOrDirectValue {

    private var _value: ReferencedOrDirectValue? = null

    private val value: ReferencedOrDirectValue
        get() {
            if (_value == null) _value = creator()
            return _value!!
        }

    override fun getKATEValue(model: KATEObject): KATEValue = value.getKATEValue(model)

    override fun toString(): String {
        return value.toString()
    }

    override fun compareTo(model: KATEObject, other: ReferencedOrDirectValue): Int {
        return value.compareTo(model, other)
    }

}