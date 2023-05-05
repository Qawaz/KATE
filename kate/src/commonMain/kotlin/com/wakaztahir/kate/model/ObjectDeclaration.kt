package com.wakaztahir.kate.model

import com.wakaztahir.kate.dsl.ModelObjectImpl
import com.wakaztahir.kate.model.model.KATEObject
import com.wakaztahir.kate.model.model.MutableKATEObject
import com.wakaztahir.kate.parser.function.parseFunctionDefinition
import com.wakaztahir.kate.parser.parseObjectDeclaration
import com.wakaztahir.kate.parser.stream.DestinationStream
import com.wakaztahir.kate.parser.variable.parseVariableDeclaration

class ObjectDeclarationModel(
    objectName: String,
    override val parent: MutableKATEObject,
    itemType: KATEType,
) : ModelObjectImpl(
    objectName = objectName,
    parent = parent,
    itemType = itemType
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
    isWriteUnprocessedTextEnabled = false,
    indentationLevel = indentationLevel
) {

    override fun parseAtDirective(): CodeGen? {
        parseVariableDeclaration()?.let { return it }
        parseFunctionDefinition(anonymousFunctionName = null)?.let { return it }
        parseObjectDeclaration()?.let { return it }
        return null
    }
}

class ObjectDeclaration(val objectName: String, val declarationBlock: ObjectDeclarationBlockSlice) : BlockContainer {

    override val isEmptyWriter: Boolean
        get() = true

    override fun getBlockValue(model: KATEObject): LazyBlock = declarationBlock
    override fun generateTo(block: LazyBlock, destination: DestinationStream) {
        declarationBlock.generateTo(destination)
        block.model.insertValue(objectName, declarationBlock.model)
    }
}