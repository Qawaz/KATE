package com.wakaztahir.kate.lexer.stream

interface LookAheadScope {

    /**
     * Requests that position be restored when the look ahead scope ends
     * By default, when the scope ends, no operation is performed
     */
    fun restorePosition()

    /**
     * Requests that position be restored and this amount is incremented to the position when the scope ends
     */
    fun restoreIncrementing(amount: Int)

}