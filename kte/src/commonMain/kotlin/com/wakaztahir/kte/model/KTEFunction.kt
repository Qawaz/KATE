package com.wakaztahir.kte.model

import com.wakaztahir.kte.model.model.KTEObject
import com.wakaztahir.kte.parser.stream.DestinationStream

abstract class KTEFunction : KTEValue {

    val parameters = mutableListOf<ReferencedValue>()

    protected abstract fun invoke(model: KTEObject, parameters: List<ReferencedValue>): KTEValue

    override fun generateTo(block: LazyBlock, destination: DestinationStream) {
        invoke(block.model, parameters).generateTo(block, destination)
    }

    override fun stringValue(indentationLevel: Int): String {
        return toString()
    }

}