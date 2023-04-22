package com.wakaztahir.kate.model.model

import com.wakaztahir.kate.model.KATEType
import com.wakaztahir.kate.model.ModelReference

interface KATEValue : ReferencedOrDirectValue {

    fun getModelReference(reference: ModelReference): KATEValue?

    fun getKnownKATEType(): KATEType

    override fun getKATEValue(model: KATEObject): KATEValue = this

    override fun getKATEType(model: KATEObject): KATEType = getKnownKATEType()

}