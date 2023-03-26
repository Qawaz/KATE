package com.wakaztahir.kte.model.model

import com.wakaztahir.kte.model.KTEValue
import com.wakaztahir.kte.model.LazyBlock
import com.wakaztahir.kte.model.ReferencedValue
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