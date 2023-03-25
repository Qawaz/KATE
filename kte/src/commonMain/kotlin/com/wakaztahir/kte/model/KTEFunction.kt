package com.wakaztahir.kte.model

import com.wakaztahir.kte.dsl.ModelValue
import com.wakaztahir.kte.model.model.KTEList
import com.wakaztahir.kte.model.model.KTEObject
import com.wakaztahir.kte.parser.stream.DestinationStream
import com.wakaztahir.kte.parser.stream.LanguageDestination

abstract class KTEFunction : KTEValue {

    val parameters = mutableListOf<ReferencedValue>()

    protected abstract fun invoke(model: KTEObject, parameters: List<ReferencedValue>): ModelValue

    override fun generateTo(block: LazyBlock, destination: DestinationStream) {
        invoke(block.model, parameters).value.generateTo(block, destination)
    }

    override fun stringValue(indentationLevel: Int): String {
        return toString()
    }

}