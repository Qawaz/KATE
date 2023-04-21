package com.wakaztahir.kate.dsl

import com.wakaztahir.kate.ScopedModelObjectName
import com.wakaztahir.kate.model.KATEType
import com.wakaztahir.kate.model.model.KATEValue
import com.wakaztahir.kate.model.ModelReference
import com.wakaztahir.kate.model.model.MutableKATEObject

class ScopedModelObject(override val parent: MutableKATEObject) : ModelObjectImpl(
    objectName = ScopedModelObjectName,
    itemType = KATEType.Any,
    parent = parent
) {

    override fun toString(): String {
        return super.toString() + " extends " + parent.toString()
    }


}