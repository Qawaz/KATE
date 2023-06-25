package com.wakaztahir.kate.lexer.stream

open class TextSourceStream(
    protected val sourceCode: CharSequence,
    private val startPosition: Int = 0,
    private val endPosition: Int = sourceCode.length
) : SourceStream {

    override var pointer: Int = startPosition

    override val currentChar: Char
        get() = sourceCode[pointer]

    override var lineNumber: Int = 1
        protected set

    override var columnNumber: Int = 1
        protected set

    override val hasEnded get() = pointer >= endPosition

    protected fun setStreamPointer(position: Int): Boolean {
        @Suppress("ConvertTwoComparisonsToRangeCheck")
        return if (position >= startPosition && position <= endPosition) {
            pointer = position
            if (!hasEnded && currentChar == '\n') {
                lineNumber++
                columnNumber = 1
            } else {
                columnNumber++
            }
            true
        } else {
            false
        }
    }

    override fun <T> lookAhead(block: LookAheadScope.() -> T): T {
        val previous = pointer
        var restoreAt: Int? = null
        val scope = object : LookAheadScope {
            override fun restorePosition() {
                restoreAt = previous
            }

            override fun restoreIncrementing(amount: Int) {
                restoreAt = previous + amount
            }
        }
        val item = block(scope)
        restoreAt?.let { setStreamPointer(it) }
        return item
    }

    override fun lookAhead(offset: Int): Char? {
        val position = pointer + offset
        return if (position < endPosition) {
            sourceCode[position]
        } else {
            null
        }
    }

    override fun lookAhead(offset: Int, length: Int): String? {
        val start = pointer + offset
        val stop = start + length
        return if (stop <= endPosition) {
            sourceCode.substring(start, stop)
        } else {
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