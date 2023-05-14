package com.wakaztahir.kate.parser

import com.wakaztahir.kate.model.CodeGen
import com.wakaztahir.kate.parser.stream.DestinationStream
import com.wakaztahir.kate.parser.stream.TextDestinationStream
import com.wakaztahir.kate.tokenizer.NodeTokenizer

open class ParsedBlock(val codeGens: List<CodeGenRange>) : CodeGen {

    class CodeGenRange(val gen: CodeGen, val start: Int, val end: Int)

    override fun <T> selectNode(tokenizer: NodeTokenizer<T>): T = tokenizer.block

    override fun generateTo(destination: DestinationStream) {
        for (range in codeGens) {
            range.gen.generateTo(destination = destination)
        }
    }

    fun generateToText() : String {
        val stream = TextDestinationStream()
        generateTo(stream)
        return stream.getValue()
    }

}