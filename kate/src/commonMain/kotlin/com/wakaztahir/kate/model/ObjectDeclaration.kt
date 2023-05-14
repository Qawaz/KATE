package com.wakaztahir.kate.model

import com.wakaztahir.kate.dsl.ModelObjectImpl
import com.wakaztahir.kate.model.model.MutableKATEObject
import com.wakaztahir.kate.parser.ObjectDeclarationParsedBlock
import com.wakaztahir.kate.parser.function.parseFunctionDefinition
import com.wakaztahir.kate.parser.parseObjectDeclaration
import com.wakaztahir.kate.parser.stream.DestinationStream
import com.wakaztahir.kate.parser.variable.parseVariableDeclaration
import com.wakaztahir.kate.tokenizer.NodeTokenizer

class ObjectDeclarationModel(
    objectName: String,
    override val parent: MutableKATEObject,
) : ModelObjectImpl(
    objectName = objectName,
    parent = parent
)

class ObjectDeclarationBlockSlice(
    parentBlock: LazyBlock,
    startPointer: Int,
    length: Int,
    blockEndPointer: Int,
    override val model: ObjectDeclarationModel,
    indentationLevel: Int
) : LazyBlockSlice(
    parentBlock = parentBlock,
    startPointer = startPointer,
    length = length,
    blockEndPointer = blockEndPointer,
    model = model,
    isDefaultNoRaw = false,
    indentationLevel = indentationLevel
) {

    override fun parseAtDirective(): CodeGen? {
        parseVariableDeclaration()?.let { return it }
        parseFunctionDefinition(anonymousFunctionName = null)?.let { return it }
        parseObjectDeclaration()?.let { return it }
        return null
    }
}

class ObjectDeclaration(
    val objectName: String,
    val itemsType: KATEType?,
    val declarationBlock: ObjectDeclarationParsedBlock,
    val model : MutableKATEObject,
) : BlockContainer {

    override val isEmptyWriter: Boolean
        get() = true

    override fun <T> selectNode(tokenizer: NodeTokenizer<T>): T = tokenizer.objectDeclaration
    override fun generateTo(destination: DestinationStream) {
        declarationBlock.generateTo(destination)
        model.insertValue(objectName, declarationBlock.model)
        itemsType?.let { model.setExplicitType(objectName, KATEType.Object(it)) }
    }
}