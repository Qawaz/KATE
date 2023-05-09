package com.wakaztahir.kate.parser

import com.wakaztahir.kate.model.CodeGen
import com.wakaztahir.kate.model.LazyBlock
import com.wakaztahir.kate.parser.stream.DestinationStream
import com.wakaztahir.kate.tokenizer.NodeTokenizer

class ParsedBlock(val codeGens: List<CodeGenRange>) : CodeGen {

    class CodeGenRange(val gen: CodeGen, val start: Int, val end: Int)

    override fun <T> selectNode(tokenizer: NodeTokenizer<T>): T = tokenizer.block

    override fun generateTo(block: LazyBlock, destination: DestinationStream) {
        for (range in codeGens) {
            range.gen.generateTo(block = block, destination = destination)
        }
    }

}