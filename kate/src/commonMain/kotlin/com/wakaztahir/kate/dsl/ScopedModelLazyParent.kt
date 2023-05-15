package com.wakaztahir.kate.dsl

import com.wakaztahir.kate.ScopedModelObjectName
import com.wakaztahir.kate.model.model.MutableKATEObject

class ScopedModelLazyParent(private val provider: () -> MutableKATEObject) : ModelObjectImpl(
    objectName = ScopedModelObjectName,
    parent = null
) {

    override val parent: MutableKATEObject
        get() = provider()

    override fun toString(): String {
        return super.toString() + " extends " + parent.toString()
    }

}