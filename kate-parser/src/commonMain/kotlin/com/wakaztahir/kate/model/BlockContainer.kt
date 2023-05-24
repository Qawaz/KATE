package com.wakaztahir.kate.model

import com.wakaztahir.kate.parser.block.ParsedBlock

interface BlockContainer : AtDirective {

    override val expectSpaceOrNewLineWithIndentationAfterwards: Boolean get() = true

    val parsedBlock : ParsedBlock

}