package com.wakaztahir.kate.lexer.lexers.nodes

import com.wakaztahir.kate.lexer.tokens.dynamic.ValueToken

data class EmbeddingToken(val path: String, val embedOnce: Boolean)

data class PlaceholderCall(val name: String, val definitionName: String?, val param: ValueToken?)

data class PlaceholderUse(val name : String,val definitionName : String)