package com.wakaztahir.kte.model.model

import com.wakaztahir.kte.model.CodeGen
import com.wakaztahir.kte.model.LazyBlock
import com.wakaztahir.kte.parser.stream.DestinationStream

object KTEUnit : ReferencedValue, CodeGen {

    override fun stringValue(indentationLevel: Int): String {
        return "KTEUnit"
    }

    override fun toString(): String {
        return "KTEUnit"
    }

    override fun generateTo(block: LazyBlock, destination: DestinationStream) {
        // Generates nothing
    }

    override fun compareTo(model: KTEObject, other: KTEValue): Int {
        throw IllegalStateException("$this cannot be compared to $other")
    }

}