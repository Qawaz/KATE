package com.wakaztahir.kate.lexer.lexers.nodes

import com.wakaztahir.kate.lexer.tokens.dynamic.ValueToken
import com.wakaztahir.kate.model.expression.ArithmeticOperatorType

data class EmbeddingToken(val path: String, val embedOnce: Boolean)

data class PlaceholderCall(val name: String, val definitionName: String?, val param: ValueToken?)

data class PlaceholderUse(val name: String, val definitionName: String)

data class VariableAssignment(
    val variableName: String,
    val arithmeticOperatorType: ArithmeticOperatorType?,
    val variableValue: ValueToken
)