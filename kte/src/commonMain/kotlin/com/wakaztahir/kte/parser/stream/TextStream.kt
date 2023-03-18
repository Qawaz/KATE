package com.wakaztahir.kte.parser.stream

class TextStream(private val sourceCode: String) : SourceStream() {

    override val streamSize: Int = sourceCode.length
    override var pointer: Int = 0

    override val currentChar: Char
        get() = sourceCode[pointer]

    override fun incrementPointer(): Boolean {
        return if (pointer + 1 <= streamSize) {
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