package com.wakaztahir.kte.model

import com.wakaztahir.kte.dsl.ModelObjectImpl
import com.wakaztahir.kte.model.model.KTEObject
import com.wakaztahir.kte.model.model.MutableKTEObject
import com.wakaztahir.kte.parser.parseVariableDeclaration
import com.wakaztahir.kte.parser.stream.DestinationStream
import com.wakaztahir.kte.parser.stream.SourceStream

class ObjectDeclarationBlockSlice(
    source: SourceStream,
    startPointer: Int,
    length: Int,
    blockEndPointer: Int,
    model: MutableKTEObject,
) : LazyBlockSlice(
    source = source,
    startPointer = startPointer,
    length = length,
    blockEndPointer = blockEndPointer,
    model = model,
    allowTextOut = false,
) {

    private var isCustomObjectSet = false

    override var model: MutableKTEObject = model
        private set

    override fun generateTo(destination: DestinationStream) {
        require(isCustomObjectSet) {
            "Custom object must be set before using object declaration block slice"
        }
        super.generateTo(destination)
        isCustomObjectSet = false
    }

    fun setCustomObject(model: MutableKTEObject) {
        this.model = model
        isCustomObjectSet = true
    }

    override fun parseAtDirective(): CodeGen? {
        return source.parseVariableDeclaration()
    }
}

class ObjectDeclaration(val objectName: String, val declarationBlock: ObjectDeclarationBlockSlice) : AtDirective,
    BlockContainer {
    override fun getBlockValue(model: KTEObject): LazyBlock = declarationBlock
    override fun generateTo(block: LazyBlock, destination: DestinationStream) {
        val mutableObject = ModelObjectImpl(objectName)
        declarationBlock.setCustomObject(mutableObject)
        declarationBlock.generateTo(destination)
        block.model.putValue(objectName, mutableObject)
    }
}