package com.wakaztahir.kate.parser

import com.wakaztahir.kate.model.*
import com.wakaztahir.kate.model.model.*
import com.wakaztahir.kate.parser.stream.DestinationStream
import com.wakaztahir.kate.parser.stream.SourceStream
import com.wakaztahir.kate.parser.stream.increment
import com.wakaztahir.kate.parser.stream.parseTextWhile

class FunctionSlice(
    parentBlock: LazyBlock,
    startPointer: Int,
    length: Int,
    blockEndPointer: Int,
    model: MutableKTEObject,
    indentationLevel: Int
) : PartialRawLazyBlockSlice(
    parentBlock = parentBlock,
    startPointer = startPointer,
    length = length,
    blockEndPointer = blockEndPointer,
    model = model,
    indentationLevel = indentationLevel
) {

    var returnedValue: KTEValue? = null
    private var hasBroken = false

    override fun canIterate(): Boolean = super.canIterate() && !hasBroken

    constructor(slice: LazyBlockSlice) : this(
        parentBlock = slice.parentBlock,
        startPointer = slice.startPointer,
        length = slice.length,
        blockEndPointer = slice.blockEndPointer,
        model = slice.model,
        indentationLevel = slice.indentationLevel
    )


    override fun parseNestedAtDirective(block: LazyBlock): CodeGen? {
        block.parseFunctionReturnValue()?.let {
            returnedValue = it
            hasBroken = true
            return KTEUnit
        }
        return super.parseNestedAtDirective(block)
    }
}

class FunctionDefinition(val slice: FunctionSlice, val functionName: String, val parameterNames: List<String>?) :
    CodeGen, BlockContainer {

    override fun getBlockValue(model: KTEObject): LazyBlock {
        return slice
    }

    private inline fun KTEObject.forEachParam(block: MutableKTEObject.(String, Int) -> Unit) {
        (this as? MutableKTEObject)?.let {
            if (parameterNames != null) {
                var i = 0
                for (param in parameterNames) {
                    block(it, param, i)
                    i++
                }
            }
        }
    }

    override fun generateTo(block: LazyBlock, destination: DestinationStream) {
        block.model.putValue(functionName, object : KTEFunction() {
            override fun invoke(model: KTEObject, invokedOn: KTEValue, parameters: List<ReferencedValue>): KTEValue {
                slice.model.forEachParam { paramName, index ->
                    if (index < parameters.size) {
                        putValue(paramName, parameters[index].getKTEValue(model))
                    } else {
                        throw IllegalStateException("function expects ${parameterNames?.size} parameters and not ${parameters.size}")
                    }
                }
                slice.generateTo(destination)
                val returned = slice.returnedValue?.getKTEValue(slice.model) ?: KTEUnit
                slice.model.forEachParam { paramName, _ -> removeKey(paramName) }
                return returned
            }

            override fun toString(): String = "$functionName()"
        })
    }
}

private fun LazyBlock.parseFunctionReturnValue(): ReferencedValue? {
    if (source.currentChar == '@' && source.increment("@return ")) {
        return source.parseAnyExpressionOrValue()
    }
    return null
}

private fun SourceStream.parseParametersNames(): List<String>? {
    if (increment('(')) {
        val parameters = mutableListOf<String>()
        do {
            val paramName = parseTextWhile { currentChar.isVariableName() }
            if (paramName.isNotEmpty()) parameters.add(paramName)
        } while (increment(','))
        if (parameters.isEmpty()) return null
        return parameters
    }
    if (!increment(')')) {
        throw IllegalStateException("expected ')' got ${source.currentChar} in function parameter definition")
    }
    return null
}

fun LazyBlock.parseFunctionDefinition(): FunctionDefinition? {
    if (source.currentChar == '@' && source.increment("@function ")) {
        val functionName = source.parseTextWhile { currentChar.isVariableName() }
        if (functionName.isEmpty()) {
            throw IllegalStateException("functionName cannot be empty")
        }
        source.increment(' ')
        val parameters = source.parseParametersNames()
        val slice = parseBlockSlice(
            startsWith = "@function",
            endsWith = "@end_function",
            allowTextOut = false,
            inheritModel = false
        )
        return FunctionDefinition(
            slice = FunctionSlice(slice = slice),
            functionName = functionName,
            parameterNames = parameters
        )
    }
    return null
}