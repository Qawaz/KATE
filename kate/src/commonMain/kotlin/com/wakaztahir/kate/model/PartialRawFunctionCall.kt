package com.wakaztahir.kate.model

import com.wakaztahir.kate.model.model.MutableKATEObject
import com.wakaztahir.kate.model.model.ReferencedOrDirectValue
import com.wakaztahir.kate.parser.stream.DestinationStream
import com.wakaztahir.kate.tokenizer.NodeTokenizer

class PartialRawFunctionCall(val value : ReferencedOrDirectValue) : CodeGen {

    override fun <T> selectNode(tokenizer: NodeTokenizer<T>): T = tokenizer.partialRawFunctionCall

    override fun generateTo(model: MutableKATEObject, destination: DestinationStream) {
        value.getKATEValue(model)
    }

}