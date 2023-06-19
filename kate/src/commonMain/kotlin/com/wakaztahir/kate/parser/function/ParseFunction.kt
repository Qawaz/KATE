package com.wakaztahir.kate.parser.function

import com.wakaztahir.kate.dsl.ScopedModelLazyParent
import com.wakaztahir.kate.model.*
import com.wakaztahir.kate.model.model.*
import com.wakaztahir.kate.parser.*
import com.wakaztahir.kate.parser.parseAnyExpressionOrValue
import com.wakaztahir.kate.parser.stream.*
import com.wakaztahir.kate.parser.stream.increment
import com.wakaztahir.kate.parser.stream.parseTextWhile
import com.wakaztahir.kate.parser.variable.isVariableName
import com.wakaztahir.kate.parser.variable.parseKATEType
import com.wakaztahir.kate.tokenizer.NodeTokenizer

class FunctionReturn(val block: FunctionParsedBlock, val value: ReferencedOrDirectValue) : CodeGen {

    override fun <T> selectNode(tokenizer: NodeTokenizer<T>): T = tokenizer.functionReturn

    override fun generateTo(destination: DestinationStream) {
        block.returnedValues[block.invocationNumber] = value.getKATEValue()
        block.haltGenFlag = true
        if (block.currentGen is BlockContainer) {
            (block.currentGen as BlockContainer).parsedBlock.onFunctionReturn()
        }
    }

}

class FunctionParsedBlock(
    parentProvider: ModelProvider,
    provider: ModelProvider.Changeable,
    codeGens: List<CodeGenRange>
) : NestableInvocationBlock(
    parentProvider = parentProvider, provider = provider, codeGens = codeGens
) {
    val returnedValues = hashMapOf<Int, KATEValue>()
    var currentGen: CodeGen? = null
    fun endInvocationAndGetReturnedValue(): KATEValue {
        val returnedValue = returnedValues.remove(invocationNumber)?.getKATEValue() ?: KATEUnit
        endInvocation()
        return returnedValue
    }

    override fun generateTo(destination: DestinationStream) {
        haltGenFlag = false
        for (gen in codeGens) {
            if (haltGenFlag) break
            currentGen = gen.gen
            gen.gen.generateTo(destination)
        }
    }
}

class FunctionSlice(
    parentBlock: LazyBlock,
    startPointer: Int,
    length: Int,
    blockEndPointer: Int,
    override val provider: ModelProvider.Changeable,
    indentationLevel: Int
) : LazyBlockSlice(
    parentBlock = parentBlock,
    startPointer = startPointer,
    length = length,
    blockEndPointer = blockEndPointer,
    provider = provider,
    indentationLevel = indentationLevel,
    isDefaultNoRaw = false
) {

    private var parseTimes = 0

    private val parsedBlock =
        FunctionParsedBlock(parentProvider = parentBlock.provider, provider = provider, codeGens = mutableListOf())

    override fun parse(): FunctionParsedBlock {
        parseTimes++
        if (parseTimes > 1) throw IllegalStateException("one instance can parse one block")
        val parsed = super.parse()
        (parsedBlock.codeGens as MutableList).addAll(parsed.codeGens)
        return parsedBlock
    }

    override fun parseNestedAtDirective(block: LazyBlock): CodeGen? {
        block.parseFunctionReturn()?.let { return FunctionReturn(parsedBlock, it) }
        block.parseDefaultNoRaw()?.let { return it }
        return super.parseNestedAtDirective(block)
    }

}

abstract class KATERecursiveFunction(
    val parsedBlock: FunctionParsedBlock,
    val parameterNames: List<String>?,
    parameterTypes: List<KATEType>?,
    returnedType: KATEType,
) : KATEFunction(returnedType, parameterTypes) {

    constructor(
        slice: FunctionParsedBlock,
        parameterNameAndTypes: Map<String, KATEType>?,
        returnedType: KATEType,
    ) : this(
        parsedBlock = slice,
        parameterNames = parameterNameAndTypes?.keys?.toList(),
        parameterTypes = parameterNameAndTypes?.values?.toList(),
        returnedType = returnedType
    )

    var destination: DestinationStream? = null

    private inline fun KATEObject.forEachParam(block: MutableKATEObject.(String, KATEType, Int) -> Unit) {
        (this as? MutableKATEObject)?.let {
            if (parameterNames != null) {
                var i = 0
                while (i < parameterNames.size) {
                    val param = parameterNames[i]
                    block(it, param, parameterTypes?.get(i)!!, i)
                    i++
                }
            }
        }
    }

    private fun putParametersIntoBlock(parameters: List<Pair<KATEValue, KATEType?>>) {
        parsedBlock.provider.model.forEachParam { paramName, paramType, index ->
            if (index < parameters.size) {
                parameters[index].let {
                    require(insertValue(paramName, it.first)) {
                        "couldn't insert function(${parsedBlock.invocationNumber}) parameter with name $paramName = $it into model $this"
                    }
//                    print("PARAM $paramName = ${it.first} FROM (${(parameters[index])}) , ")
                    it.second?.let { type -> setExplicitType(paramName, type) }
                }
//                println()
            } else {
                throw IllegalStateException("function expects ${parameterNames?.size} parameters and not ${parameters.size}")
            }
        }
    }

    private fun generateNow(parameters: List<Pair<KATEValue, KATEType?>>): KATEValue {
        parsedBlock.startInvocation()
        putParametersIntoBlock(parameters = parameters)
        parsedBlock.generateTo(destination!!)
        return parsedBlock.endInvocationAndGetReturnedValue()
    }

    override fun invoke(
        model: KATEObject,
        invokedOn: KATEValue,
        explicitType: KATEType?,
        parameters: List<ReferencedOrDirectValue>
    ): ReferencedOrDirectValue {
        return generateNow(parameters.map { it.getKATEValueAndType() })
    }

}

class FunctionDefinition(
    override val parsedBlock: FunctionParsedBlock,
    val functionName: String,
    val definitionModel: ModelProvider,
    parameterNames: Map<String, KATEType>?,
    returnedType: KATEType
) : BlockContainer {

    override fun <T> selectNode(tokenizer: NodeTokenizer<T>): T = tokenizer.functionDefinition

    val definition =
        object : KATERecursiveFunction(slice = parsedBlock, parameterNames, returnedType) {
            override fun toString(): String = functionName + ' ' + super.toString()
        }

    override fun generateTo(destination: DestinationStream) {
        definition.destination = destination
        definitionModel.model.insertValue(functionName, definition)
    }
}

private fun LazyBlock.parseFunctionReturn(): ReferencedOrDirectValue? {
    if (source.currentChar == '@' && source.increment("@return ")) {
        return parseAnyExpressionOrValue(
            parseDirectRefs = true
        ) ?: KATEUnit
    }
    return null
}

private fun ParserSourceStream.parseFunctionParameters(): Map<String, KATEType>? {
    if (increment('(')) {
        val parameters = mutableMapOf<String, KATEType>()
        do {
            val paramName = parseTextWhile { currentChar.isVariableName() }
            if (paramName.isEmpty()) continue
            escapeSpaces()
            val paramType = if (increment(':')) {
                escapeSpaces()
                parseKATEType(parseMetadata = false) ?: KATEType.Any
            } else KATEType.Any
            parameters[paramName] = paramType
        } while (increment(','))
        if (!increment(')')) {
            throw IllegalStateException("expected ')' got $currentChar in function parameter definition")
        }
        if (parameters.isEmpty()) return null
        return parameters
    }
    if (!increment(')')) {
        throw IllegalStateException("expected ')' got $currentChar in function parameter definition")
    }
    return null
}

fun LazyBlock.parseFunctionDefinition(anonymousFunctionName: String?): FunctionDefinition? {
    if (source.currentChar == '@' && source.increment("@function")) {
        source.increment(' ')
        val functionName: String = anonymousFunctionName
            ?: source.parseTextWhile { currentChar.isVariableName() }.also {
                if (it.isEmpty()) {
                    throw IllegalStateException("functionName cannot be empty")
                }
            }
        source.increment(' ')
        val parameters = source.parseFunctionParameters()

        val afterParametersPointer = source.pointer

        source.escapeSpaces()
        val returnedType = if (source.increment("->")) {
            source.escapeSpaces()
            source.parseKATEType(parseMetadata = false)
                ?: throw IllegalStateException("expected a type after \"->\" but got ${source.currentChar}")
        } else {
            source.setPointerAt(afterParametersPointer)
            KATEType.Any
        }

        val slice = parseBlockSlice(
            startsWith = "@function",
            endsWith = "@end_function",
            isDefaultNoRaw = false,
            provider = ModelProvider.Changeable(ScopedModelLazyParent { provider.model })
        )

        val blockParser = FunctionSlice(
            parentBlock = slice.parentBlock,
            startPointer = slice.startPointer,
            length = slice.length,
            blockEndPointer = slice.blockEndPointer,
            provider = slice.provider as ModelProvider.Changeable,
            indentationLevel = slice.indentationLevel
        )

        return FunctionDefinition(
            parsedBlock = blockParser.parse(),
            definitionModel = provider,
            functionName = functionName,
            parameterNames = parameters,
            returnedType = returnedType
        )
    }
    return null
}

fun interface KATEInvocation {
    fun invoke(
        model: KATEObject,
        invokedOn: ReferencedOrDirectValue,
        explicitType: KATEType?,
        parameters: List<ReferencedOrDirectValue>
    ): KATEValue
}

fun KATEParsedFunction(
    typeDefinition: String,
    invoke: KATEInvocation
): KATEFunction {
    val source = TextParserSourceStream("@function $typeDefinition @end_function")
    val parsed = source.block.parseFunctionDefinition(anonymousFunctionName = null)!!
    val parsedDef = parsed.definition
    return object : KATEFunction(parsedDef.returnedType, parsedDef.parameterTypes) {
        override fun invoke(
            model: KATEObject,
            invokedOn: KATEValue,
            explicitType: KATEType?,
            parameters: List<ReferencedOrDirectValue>
        ): ReferencedOrDirectValue {
            return invoke.invoke(model, invokedOn, null, parameters)
        }

        override fun toString(): String = parsed.functionName + ' ' + super.toString()
    }
}