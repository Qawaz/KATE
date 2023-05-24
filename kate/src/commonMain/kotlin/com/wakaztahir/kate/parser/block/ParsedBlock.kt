package com.wakaztahir.kate.parser.block

import com.wakaztahir.kate.model.CodeGen
import com.wakaztahir.kate.model.block.DefaultNoRawString
import com.wakaztahir.kate.parser.stream.DestinationStream
import com.wakaztahir.kate.parser.stream.TextDestinationStream
import com.wakaztahir.kate.tokenizer.NodeTokenizer

open class ParsedBlock(val codeGens: List<CodeGenRange>) : CodeGen {

    class CodeGenRange(val gen: CodeGen, val start: Int, end: Int) {
        var end: Int = end
            private set

        fun incrementEndForDefaultNoRawStringGen(char: Char) {
            (this.gen as DefaultNoRawString).stringValue += char
            this.end++
        }
    }

    // if true , break iteration of loop before next statement gets generated
    var haltGenFlag = false

    override fun <T> selectNode(tokenizer: NodeTokenizer<T>): T = tokenizer.block

    override fun generateTo(destination: DestinationStream) {
        for (range in codeGens) {
            if (haltGenFlag) return
            range.gen.generateTo(destination = destination)
        }
    }

    open fun onFunctionReturn() {
        this.haltGenFlag = true
    }

    fun generateToText(): String {
        val stream = TextDestinationStream()
        generateTo(stream)
        return stream.getValue()
    }

}