package com.wakaztahir.kate.dsl

import com.wakaztahir.kate.ScopedModelObjectName
import com.wakaztahir.kate.model.model.KTEValue
import com.wakaztahir.kate.model.ModelReference
import com.wakaztahir.kate.model.model.MutableKTEObject

class ScopedModelObject(override val parent: MutableKTEObject) : ModelObjectImpl(ScopedModelObjectName) {

    override fun getModelReference(reference: ModelReference): KTEValue? {
        return super.getModelReference(reference) ?: parent.getModelReference(reference)
    }

    override fun contains(key: String): Boolean {
        return if (super.contains(key)) {
            true
        } else {
            parent.contains(key)
        }
    }

    override fun putValue(key: String, value: KTEValue) {
        if (parent.contains(key)) {
            parent.putValue(key, value)
        } else {
            super.putValue(key, value)
        }
    }

    override fun toString(): String {
        return super.toString() + " extends " + parent.toString()
    }


}