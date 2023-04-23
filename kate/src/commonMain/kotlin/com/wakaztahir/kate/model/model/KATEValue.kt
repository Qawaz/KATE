package com.wakaztahir.kate.model.model

import com.wakaztahir.kate.model.KATEType
import com.wakaztahir.kate.model.ModelReference

interface KATEValue : ReferencedOrDirectValue {

    fun getModelReference(reference: ModelReference): KATEValue?

    fun getModelReferenceType(reference: ModelReference): KATEType? {
        return getModelReference(reference)?.getKnownKATEType()
    }

    fun getKnownKATEType(): KATEType

    override fun getKATEValue(model: KATEObject): KATEValue = this

}