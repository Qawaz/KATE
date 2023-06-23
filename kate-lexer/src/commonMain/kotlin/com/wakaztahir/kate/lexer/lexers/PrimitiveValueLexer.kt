package com.wakaztahir.kate.lexer.lexers

import com.wakaztahir.kate.lexer.Lexer
import com.wakaztahir.kate.lexer.stream.SourceStream
import com.wakaztahir.kate.lexer.tokens.TokenRange

class PrimitiveValueLexer(override val source: SourceStream) : Lexer {

    override fun lexTokenRangeAtCurrentPosition(): TokenRange? = with(source){
        TODO("Not yet implemented")
    }

    override fun getState(): Int {
        TODO("Not yet implemented")
    }

    override fun restoreState(state: Int) {
        TODO("Not yet implemented")
    }

}