package com.wakaztahir.kate.lexer.model

import com.wakaztahir.kate.lexer.model.KATEToken

data class TokenRange(val token: KATEToken, val start : Int, val end : Int)