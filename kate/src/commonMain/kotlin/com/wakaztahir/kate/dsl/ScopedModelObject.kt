package com.wakaztahir.kate.dsl

import com.wakaztahir.kate.ScopedModelObjectName
import com.wakaztahir.kate.model.model.KATEValue
import com.wakaztahir.kate.model.ModelReference
import com.wakaztahir.kate.model.model.MutableKATEObject

class ScopedModelObject(override val parent: MutableKATEObject) : ModelObjectImpl(ScopedModelObjectName) {

    override fun getModelReference(reference: ModelReference): KATEValue? {
        return super.getModelReference(reference) ?: parent.getModelReference(reference)
    }

    override fun putValue(key: String, value: KATEValue) {
        if (parent.containsInAncestors(key)) {
            parent.putValue(key, value)
        } else {
            super.putValue(key, value)
        }
    }

    override fun toString(): String {
        return super.toString() + " extends " + parent.toString()
    }


}