package com.wakaztahir.kate.lexer

import java.io.InputStream

open class InputSourceStream(val inputStream: InputStream) : SourceStream {

    override var pointer: Int = 0
        protected set

    private var currentInt: Int = readInt()

    override val currentChar: Char get() = currentInt.toChar()

    override val hasEnded: Boolean get() = currentInt == -1

    private fun readInt(): Int {
        inputStream.mark(pointer + 1)
        inputStream.skip(pointer.toLong())
        val character = inputStream.read()
        inputStream.reset()
        return character
    }

    protected fun setStreamPointer(position: Int): Boolean {
        if (position >= 0 && pointer != position) {
            pointer = position
            currentInt = readInt()
            return true
        }
        return false
    }

    override fun lookAhead(offset: Int): Char? {
        return try {
            setStreamPointer(pointer + offset)
            val char = currentChar
            setStreamPointer(pointer - offset)
            return char
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override fun lookAhead(offset: Int, length: Int): String? {
        return try {
            increment(offset)
            var text = ""
            while (pointer < length) {
                text += currentChar
                incrementPointer()
            }
            setStreamPointer(pointer - offset - length)
            text
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override fun incrementPointer(): Boolean {
        return setStreamPointer(pointer + 1)
    }

    override fun increment(amount: Int): Boolean {
        return setStreamPointer(pointer + amount)
    }

}