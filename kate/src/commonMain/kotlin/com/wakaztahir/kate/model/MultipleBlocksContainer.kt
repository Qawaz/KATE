package com.wakaztahir.kate.model

interface MultipleBlocksContainer : AtDirective {

    override val expectSpaceOrNewLineWithIndentationAfterwards: Boolean get() = true

}