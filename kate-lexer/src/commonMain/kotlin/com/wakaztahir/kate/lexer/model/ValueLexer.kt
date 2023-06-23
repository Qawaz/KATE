package com.wakaztahir.kate.lexer.model

import com.wakaztahir.kate.lexer.tokens.dynamic.ValueToken

interface ValueLexer<T : ValueToken> : DynamicTokenLexer<T>