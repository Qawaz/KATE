package com.wakaztahir.kate.parser.variable

import com.wakaztahir.kate.lexer.lexers.VariableAssignmentLexer
import com.wakaztahir.kate.lexer.tokens.StaticTokens
import com.wakaztahir.kate.model.model.MutableKATEObject
import com.wakaztahir.kate.model.*
import com.wakaztahir.kate.model.model.KATEValue
import com.wakaztahir.kate.model.model.ReferencedOrDirectValue
import com.wakaztahir.kate.lexer.stream.*
import com.wakaztahir.kate.lexer.stream.increment
import com.wakaztahir.kate.parser.*
import com.wakaztahir.kate.parser.ExpressionValue
import com.wakaztahir.kate.parser.parseArithmeticOperator
import com.wakaztahir.kate.parser.stream.DestinationStream
import com.wakaztahir.kate.parser.stream.ParserSourceStream
import com.wakaztahir.kate.tokenizer.NodeTokenizer

data class VariableAssignment(
    val variableName: String,
    val arithmeticOperatorType: ArithmeticOperatorType?,
    val variableValue: ReferencedOrDirectValue,
    val provider: ModelProvider,
) : AtDirective {

    override fun <T> selectNode(tokenizer: NodeTokenizer<T>): T = tokenizer.variableAssignment

    override val isEmptyWriter: Boolean
        get() = true

    private fun throwIt(): Nothing {
        throw IllegalStateException("error setting value of variable $variableName , couldn't get original value")
    }

    private fun getValue(model: MutableKATEObject): KATEValue {
        return if (arithmeticOperatorType == null) {
            variableValue.getKATEValue()
        } else {
            ExpressionValue(
                first = model.getModelReference(ModelReference.Property(variableName)) ?: throwIt(),
                operatorType = arithmeticOperatorType,
                second = variableValue
            ).getKATEValue()
        }
    }

    fun storeValue(model: MutableKATEObject) {
        if (!model.setValueInTreeUpwardsTypeSafely(variableName, getValue(model))) {
            throw VariableAssignmentException("couldn't assign variable $variableName because it doesn't exist")
        }
    }

    override fun generateTo(destination: DestinationStream) {
        storeValue(provider.model)
    }
}

class VariableAssignmentException : Exception {
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
}

// Assignment Left Hand Side
data class AssignmentLHS(val variableName: String, val type: ArithmeticOperatorType?) {
    override fun toString(): String = "$variableName ${type ?: ""}= "
}

internal fun ParserSourceStream.parseAssignmentLHS(isExplicitAssignment: Boolean): AssignmentLHS? {
    val previous = pointer
    if (isExplicitAssignment) {
        if (!incrementDirective(StaticTokens.SetVar)) return null
    } else {
        incrementDirective(StaticTokens.SetVar)
    }
    escapeSpaces()
    val variableName = parseTextWhile { currentChar.isVariableName() }
    escapeSpaces()
    val arithmeticOperator = parseArithmeticOperator()
    if (increment(StaticTokens.SingleEqual)) {
        val valid = isValidVariableName(variableName)
        if (valid.isSuccess) {
            return AssignmentLHS(variableName, arithmeticOperator)
        } else {
            valid.exceptionOrNull()?.let { throw VariableAssignmentException(it.message ?: "", cause = it) }
        }
    } else if (isExplicitAssignment) {
        throw IllegalStateException("expected '${StaticTokens.SingleEqual}' after left hand side of the assignment expression with variable name $variableName but got $currentChar")
    }
    if (isExplicitAssignment) {
        throw IllegalStateException()
    }
    setPointerAt(previous)
    return null
}

internal fun LazyBlock.parseVariableAssignment(): VariableAssignment? {
    val lhs = source.parseAssignmentLHS(isExplicitAssignment = isDefaultNoRaw) ?: return null
    source.escapeSpaces()
    val property = parseAnyExpressionOrValue(
        parseDirectRefs = true
    )
    return if (property != null) {
        VariableAssignment(
            variableName = lhs.variableName,
            arithmeticOperatorType = lhs.type,
            variableValue = property,
            provider = provider,
        )
    } else {
        throw VariableAssignmentException("variable value not found in variable assignment expression $lhs")
    }
}