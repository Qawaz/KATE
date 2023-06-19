package com.wakaztahir.kate.model

import com.wakaztahir.kate.model.model.ReferencedOrDirectValue
import com.wakaztahir.kate.parser.stream.DestinationStream
import com.wakaztahir.kate.parser.stream.ParserSourceStream
import com.wakaztahir.kate.tokenizer.NodeTokenizer

class DefaultNoRawExpression(
    val source: ParserSourceStream,
    val value: ReferencedOrDirectValue,
    val provider : ModelProvider,
) : CodeGen {

    override fun <T> selectNode(tokenizer: NodeTokenizer<T>): T = tokenizer.defaultNoRawExpression

    override fun generateTo(destination: DestinationStream) {
        val value = value.getKATEValue()
        PlaceholderInvocation(
            placeholderName = value.getKnownKATEType().getPlaceholderName(),
            definitionName = null,
            paramValue = value,
            placeholderManager = source.placeholderManager,
            invocationProvider = provider,
        ).generateTo(destination = destination)
    }

}