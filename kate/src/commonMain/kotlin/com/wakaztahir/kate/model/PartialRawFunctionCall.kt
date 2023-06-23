package com.wakaztahir.kate.model

import com.wakaztahir.kate.lexer.tokens.dynamic.AccessChainToken
import com.wakaztahir.kate.model.model.ReferencedOrDirectValue
import com.wakaztahir.kate.model.model.ReferencedValue
import com.wakaztahir.kate.parser.stream.DestinationStream
import com.wakaztahir.kate.tokenizer.NodeTokenizer

class PartialRawFunctionCall(val value : ReferencedOrDirectValue) : CodeGen {

    init {
        if(value is ReferencedValue) {
            value.propertyPath.lastOrNull()?.let { c -> c as? ModelReference.FunctionCall } ?: run {
                throw IllegalStateException("variable reference $value cannot be used inside @partial_raw")
            }
        }
    }

    override fun <T> selectNode(tokenizer: NodeTokenizer<T>): T = tokenizer.partialRawFunctionCall

    override fun generateTo(destination: DestinationStream) {
        value.getKATEValue()
    }

}