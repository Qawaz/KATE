package com.wakaztahir.kte.parser

import com.wakaztahir.kte.model.*
import com.wakaztahir.kte.model.model.*
import com.wakaztahir.kte.parser.stream.DestinationStream
import com.wakaztahir.kte.parser.stream.increment
import com.wakaztahir.kte.parser.stream.parseTextWhile

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

class FunctionDefinition(val slice: FunctionSlice, val functionName: String) : CodeGen {
    override fun generateTo(block: LazyBlock, destination: DestinationStream) {
        block.model.putValue(functionName, object : KTEFunction() {
            override fun invoke(model: KTEObject, invokedOn: KTEValue, parameters: List<ReferencedValue>): KTEValue {
                (model as? MutableKTEObject)?.putValue("this", KTEListImpl(parameters))
                slice.generateTo(destination)
                (model as? MutableKTEObject)?.removeKey("this")
                return slice.returnedValue ?: throw IllegalStateException("function $functionName didn't return a value")
            }

            override fun toString(): String = "$functionName()"
        })
    }
}

private fun LazyBlock.parseFunctionReturnValue(): ReferencedValue? {
    if (source.currentChar == '@' && source.increment("@return ")) {
        if (source.increment("@unit") || source.increment("@Unit")) {
            return KTEUnit
        }
        return source.parseAnyExpressionOrValue()
    }
    return null
}

fun LazyBlock.parseFunctionDefinition(): FunctionDefinition? {
    if (source.currentChar == '@' && source.increment("@function(")) {
        val functionName = source.parseTextWhile { currentChar.isVariableName() }
        if (functionName.isEmpty()) {
            throw IllegalStateException("functionName cannot be empty")
        }
        if (!source.increment(')')) {
            throw IllegalStateException("expected ')' got ${source.currentChar} in function definition")
        }
        val slice = parseBlockSlice(
            startsWith = "@function",
            endsWith = "@end_function",
            allowTextOut = false,
            inheritModel = true
        )
        return FunctionDefinition(slice = FunctionSlice(slice = slice), functionName)
    }
    return null
}