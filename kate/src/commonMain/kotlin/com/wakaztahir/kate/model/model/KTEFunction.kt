package com.wakaztahir.kate.model.model

abstract class KTEFunction : ReferencedValue {

    abstract fun invoke(model: KTEObject, invokedOn: KTEValue, parameters: List<ReferencedValue>): KTEValue

    override fun getKTEValue(model: KTEObject): KTEValue {
        throw IllegalStateException("KTEFunction should be invoked to get the value")
    }

    override fun compareTo(model: KTEObject, other: KTEValue): Int {
        throw IllegalStateException("KTEFunction should be invoked first to get the value to compare with the other")
    }

}