package com.wakaztahir.kate.model

import com.wakaztahir.kate.model.model.ReferencedOrDirectValue
import com.wakaztahir.kate.parser.stream.DestinationStream
import com.wakaztahir.kate.tokenizer.NodeTokenizer

class DefaultNoRawExpression(val value : ReferencedOrDirectValue) : CodeGen {

    override fun <T> selectNode(tokenizer: NodeTokenizer<T>): T = tokenizer.defaultNoRawExpression

    override fun generateTo(block: LazyBlock, destination: DestinationStream) {
        val value = value.getKATEValue(block.model)
        PlaceholderInvocation(
            placeholderName = value.getKnownKATEType().getPlaceholderName(),
            definitionName = null,
            paramValue = value,
            invocationEndPointer = block.source.pointer
        ).generateTo(block = block,destination = destination)
    }

}