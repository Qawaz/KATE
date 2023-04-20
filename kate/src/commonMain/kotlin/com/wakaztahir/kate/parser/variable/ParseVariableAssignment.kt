package com.wakaztahir.kate.parser.variable

import com.wakaztahir.kate.model.model.MutableKATEObject
import com.wakaztahir.kate.model.*
import com.wakaztahir.kate.model.model.KATEValue
import com.wakaztahir.kate.model.model.ReferencedValue
import com.wakaztahir.kate.parser.ArithmeticOperatorType
import com.wakaztahir.kate.parser.ExpressionValue
import com.wakaztahir.kate.parser.parseAnyExpressionOrValue
import com.wakaztahir.kate.parser.parseArithmeticOperator
import com.wakaztahir.kate.parser.stream.*
import com.wakaztahir.kate.parser.stream.increment

internal data class VariableAssignment(
    val variableName: String,
    val arithmeticOperatorType: ArithmeticOperatorType?,
    val variableValue: ReferencedValue
) : AtDirective {

    override val isEmptyWriter: Boolean
        get() = true

    private fun throwIt(): Nothing {
        throw IllegalStateException("error setting value of variable $variableName , couldn't get original value")
    }

    private fun getValue(model: MutableKATEObject): KATEValue {
        // TODO do not store value after resolving it
        // TODO store reference for referenced value
        return if (arithmeticOperatorType == null) {
            variableValue.getKATEValue(model)
        } else {
            ExpressionValue(
                first = model.getModelReference(ModelReference.Property(variableName)) ?: throwIt(),
                operatorType = arithmeticOperatorType,
                second = variableValue
            ).getKATEValue(model)
        }
    }

    fun storeValue(model: MutableKATEObject) {
        if (!model.setValue(variableName, getValue(model))) {
            throw VariableAssignmentException("couldn't assign variable $variableName because it doesn't exist")
        }
    }

    override fun generateTo(block: LazyBlock, destination: DestinationStream) {
        storeValue(block.model)
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

internal fun SourceStream.parseAssignmentLHS(isExplicitAssignment: Boolean): AssignmentLHS? {
    val previous = pointer
    if (isExplicitAssignment) {
        if (currentChar != '@' || !increment("@set_var")) return null
    } else {
        increment("@set_var")
    }
    escapeSpaces()
    val variableName = parseTextWhile { currentChar.isVariableName() }
    escapeSpaces()
    val arithmeticOperator = parseArithmeticOperator()
    if (source.increment('=')) {
        val valid = isValidVariableName(variableName)
        if (valid.isSuccess) {
            return AssignmentLHS(variableName, arithmeticOperator)
        } else {
            valid.exceptionOrNull()?.let { throw VariableAssignmentException(it.message ?: "", cause = it) }
        }
    } else if (isExplicitAssignment) {
        throw IllegalStateException("expected '=' after left hand side of the assignment expression with variable name $variableName but got $currentChar")
    }
    if(isExplicitAssignment){
        throw IllegalStateException()
    }
    setPointerAt(previous)
    return null
}

internal fun LazyBlock.parseVariableAssignment(): VariableAssignment? {
    val lhs = source.parseAssignmentLHS(isExplicitAssignment = isWriteUnprocessedTextEnabled) ?: return null
    source.escapeSpaces()
    val property = source.parseAnyExpressionOrValue(
        parseFirstStringOrChar = true,
        parseNotFirstStringOrChar = true,
        parseDirectRefs = true,
        allowAtLessExpressions = true
    )
    return if (property != null) {
        VariableAssignment(
            variableName = lhs.variableName,
            arithmeticOperatorType = lhs.type,
            variableValue = property
        )
    } else {
        throw VariableAssignmentException("variable value not found in variable assignment expression $lhs")
    }
}