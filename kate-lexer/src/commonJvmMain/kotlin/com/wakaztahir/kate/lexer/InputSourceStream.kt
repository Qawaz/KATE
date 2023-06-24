package com.wakaztahir.kate.lexer

import com.wakaztahir.kate.lexer.stream.LookAheadScope
import com.wakaztahir.kate.lexer.stream.SourceStream
import java.io.InputStream

open class InputSourceStream(val inputStream: InputStream) : SourceStream {

    override var pointer: Int = 0
        protected set

    private var currentInt: Int = readInt()

    override val currentChar: Char get() = currentInt.toChar()

    override var lineNumber: Int = 1
        protected set

    override var columnNumber: Int = 1
        protected set

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
            currentChar.let { current ->
                if (current == '\n') {
                    lineNumber++
                    columnNumber = 1
                } else {
                    columnNumber++
                }
            }
            return true
        }
        return false
    }

    override fun <T> lookAhead(block: LookAheadScope.() -> T): T {
        val previousPosition = pointer
        var restoreAt: Int? = null
        val item = block(object : LookAheadScope {
            override fun restorePosition() {
                restoreAt = previousPosition
            }

            override fun restoreIncrementing(amount: Int) {
                restoreAt = previousPosition + amount
            }
        })
        restoreAt?.let { setStreamPointer(it) }
        return item
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
            incrementPointer(offset)
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

    override fun incrementPointer(amount: Int): Boolean {
        return setStreamPointer(pointer + amount)
    }

}