package com.wakaztahir.kte.model.model

import com.wakaztahir.kte.model.LazyBlock
import com.wakaztahir.kte.model.ModelReference
import com.wakaztahir.kte.parser.stream.DestinationStream

abstract class KTEFunction : ReferencedValue {

    val parameters = mutableListOf<ReferencedValue>()

    abstract fun invoke(model: KTEObject, invokedOn: KTEValue, parameters: List<ReferencedValue>): KTEValue

    override fun getKTEValue(model: KTEObject): KTEValue {
        throw IllegalStateException("KTEFunction should be invoked to get the value")
    }

    override fun compareTo(model: KTEObject, other: KTEValue): Int {
        throw IllegalStateException("KTEFunction should be invoked first to get the value to compare with the other")
    }

    override fun stringValue(indentationLevel: Int): String {
        return toString()
    }

}