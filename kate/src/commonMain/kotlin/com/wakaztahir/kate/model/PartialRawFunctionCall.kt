package com.wakaztahir.kate.model

import com.wakaztahir.kate.model.model.KATEUnit
import com.wakaztahir.kate.model.model.ReferencedOrDirectValue
import com.wakaztahir.kate.parser.stream.DestinationStream
import com.wakaztahir.kate.tokenizer.NodeTokenizer

class PartialRawFunctionCall(val value : ReferencedOrDirectValue) : CodeGen {

    override fun <T> selectNode(tokenizer: NodeTokenizer<T>): T = tokenizer.partialRawFunctionCall

    override fun generateTo(block: LazyBlock, destination: DestinationStream) {
        value.getKATEValue(block.model)
        PlaceholderInvocation(
            placeholderName = KATEType.Unit.getKATEType(),
            definitionName = null,
            paramValue = KATEUnit,
            invocationEndPointer = block.source.pointer
        ).generateTo(block = block,destination = destination)
    }

}