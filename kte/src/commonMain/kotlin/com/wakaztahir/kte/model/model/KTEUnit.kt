package com.wakaztahir.kte.model.model

import com.wakaztahir.kte.model.LazyBlock
import com.wakaztahir.kte.parser.stream.DestinationStream

object KTEUnit : KTEValue {

    override fun stringValue(indentationLevel: Int): String {
        return "KTEUnit"
    }

    override fun toString(): String {
        return "KTEUnit"
    }

    override fun generateTo(block: LazyBlock, destination: DestinationStream) {
        // Generates nothing
    }

}