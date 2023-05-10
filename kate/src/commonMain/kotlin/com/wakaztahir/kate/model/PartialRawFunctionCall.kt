package com.wakaztahir.kate.model

import com.wakaztahir.kate.model.model.KATEUnit
import com.wakaztahir.kate.model.model.MutableKATEObject
import com.wakaztahir.kate.model.model.ReferencedOrDirectValue
import com.wakaztahir.kate.parser.stream.DestinationStream
import com.wakaztahir.kate.parser.stream.SourceStream
import com.wakaztahir.kate.tokenizer.NodeTokenizer

class PartialRawFunctionCall(val value : ReferencedOrDirectValue,val source : SourceStream) : CodeGen {

    override fun <T> selectNode(tokenizer: NodeTokenizer<T>): T = tokenizer.partialRawFunctionCall

    override fun generateTo(model: MutableKATEObject, destination: DestinationStream) {
        value.getKATEValue(model)
        PlaceholderInvocation(
            placeholderName = KATEType.Unit.getKATEType(),
            definitionName = null,
            paramValue = KATEUnit,
            invocationEndPointer = source.pointer,
            placeholderManager = source.placeholderManager,
            source = source
        ).generateTo(model = model, destination = destination)
    }

}