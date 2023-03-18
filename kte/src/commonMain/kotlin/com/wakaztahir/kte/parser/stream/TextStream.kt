package com.wakaztahir.kte.parser.stream

class TextStream(private val sourceCode: String) : SourceStream() {

    override var pointer: Int = 0

    override val currentChar: Char
        get() = sourceCode[pointer]

    override val hasEnded get() = sourceCode.length == pointer

    override fun incrementPointer(): Boolean {
        return if (pointer + 1 <= sourceCode.length) {
            pointer += 1
            true
        } else {
            false
        }
    }

    override fun decrementPointer(decrease: Int): Boolean {
        if (decrease == 0) return true
        return if (pointer - decrease >= 0) {
            pointer -= decrease
            true
        } else {
            false
        }
    }

}