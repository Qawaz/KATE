package com.wakaztahir.kte.model.model

import com.wakaztahir.kte.model.LazyBlock
import com.wakaztahir.kte.parser.stream.DestinationStream

abstract class KTEFunction : ReferencedValue {

    val parameters = mutableListOf<ReferencedValue>()
    var invokeOnly = false

    protected abstract fun invoke(model: KTEObject, parameters: List<ReferencedValue>): KTEValue

    override fun getKTEValue(model: KTEObject): KTEValue {
        return if (invokeOnly) {
            invoke(model, parameters)
            KTEUnit
        } else {
            invoke(model, parameters)
        }
    }

    override fun generateTo(block: LazyBlock, destination: DestinationStream) {
        getKTEValue(block.model).generateTo(block,destination)
    }

    override fun stringValue(indentationLevel: Int): String {
        return toString()
    }

}