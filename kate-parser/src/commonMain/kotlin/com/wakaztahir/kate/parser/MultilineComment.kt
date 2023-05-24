package com.wakaztahir.kate.parser

import com.wakaztahir.kate.model.CodeGen
import com.wakaztahir.kate.parser.stream.DestinationStream
import com.wakaztahir.kate.tokenizer.NodeTokenizer

class MultilineComment(val commentText: String) : CodeGen {

    override fun <T> selectNode(tokenizer: NodeTokenizer<T>): T = tokenizer.multilineComment

    override fun generateTo(destination: DestinationStream) {
        // comment doesn't generate anything
    }

}