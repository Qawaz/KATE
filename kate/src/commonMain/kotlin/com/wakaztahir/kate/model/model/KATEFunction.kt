package com.wakaztahir.kate.model.model

import com.wakaztahir.kate.model.KATEType
import com.wakaztahir.kate.model.ModelReference

abstract class KATEFunction : KATEValue {

    abstract fun invoke(model: KATEObject,path : List<ModelReference>,pathIndex : Int, invokedOn: KATEValue, parameters: List<KATEValue>): KATEValue

    override fun getModelReference(reference: ModelReference): KATEValue? {
        throw IllegalStateException("KATEFunction should be invoked to get the reference")
    }

    override fun getKnownKATEType(): KATEType? {
        throw IllegalStateException("KATEFunction should be invoked to get the value")
    }

    override fun getKATEValue(model: KATEObject): KATEValue {
        throw IllegalStateException("KATEFunction should be invoked to get the value")
    }

    override fun getKATEType(model: KATEObject): KATEType {
        throw IllegalStateException("KATEFunction should be invoked to get the value and then type")
    }

    override fun compareTo(model: KATEObject, other: KATEValue): Int {
        throw IllegalStateException("KATEFunction should be invoked first to get the value to compare with the other")
    }

}