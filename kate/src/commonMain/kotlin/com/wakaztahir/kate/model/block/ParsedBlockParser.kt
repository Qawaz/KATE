package com.wakaztahir.kate.model.block

import com.wakaztahir.kate.model.CodeGen
import com.wakaztahir.kate.model.LazyBlock
import com.wakaztahir.kate.parser.stream.DestinationStream
import com.wakaztahir.kate.parser.stream.TextDestinationStream

@Suppress("unused")
class ParsedBlockParser(private val block: LazyBlock) : LazyBlock by block {

    val codeGens = mutableListOf<CodeGen>()

    fun parseCompletely(destination: TextDestinationStream = TextDestinationStream()): List<CodeGen> {
        generateTo(destination)
        return codeGens
    }

    override fun writeDirective(directive: CodeGen, destination: DestinationStream) {
        codeGens.add(directive)
    }

    override fun writeCurrentChar(destination: DestinationStream) {
        if (codeGens.isEmpty() || codeGens.last() !is DefaultNoRawString) {
            codeGens.add(DefaultNoRawString("${source.currentChar}"))
        } else {
            val last = codeGens.last() as DefaultNoRawString
            last.stringValue += source.currentChar
        }
    }

}