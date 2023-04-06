package com.wakaztahir.kate.model.model

import com.wakaztahir.kate.model.ModelReference

abstract class KATEFunction : ReferencedValue {

    abstract fun invoke(model: KATEObject, invokedOn: KATEValue, parameters: List<ReferencedValue>): KATEValue

    override fun getModelReference(reference: ModelReference): KATEValue? {
        throw IllegalStateException("KTEFunction should be invoked to get the reference")
    }

    override fun getKTEValue(model: KATEObject): KATEValue {
        throw IllegalStateException("KTEFunction should be invoked to get the value")
    }

    override fun compareTo(model: KATEObject, other: KATEValue): Int {
        throw IllegalStateException("KTEFunction should be invoked first to get the value to compare with the other")
    }

}