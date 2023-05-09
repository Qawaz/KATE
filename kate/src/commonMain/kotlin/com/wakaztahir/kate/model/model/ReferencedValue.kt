package com.wakaztahir.kate.model.model

import com.wakaztahir.kate.model.ModelReference

interface ReferencedValue : ReferencedOrDirectValue {

    val propertyPath: List<ModelReference>

    val referenceModel: KATEObject

}