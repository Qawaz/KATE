package com.wakaztahir.kate.model.model

import com.wakaztahir.kate.model.*
import com.wakaztahir.kate.parser.stream.DestinationStream
import com.wakaztahir.kate.runtime.KATEValueImplementation

object KATEUnit : KATEValue, CodeGen {

    override fun toString(): String = "KATEUnit"

    override fun getModelReference(reference: ModelReference): KATEValue? {
        return KATEValueImplementation.propertyMap[reference.name]
    }

    override fun getKnownKATEType(): KATEType = KATEType.Unit()

    override fun getKATEType(model: KATEObject): KATEType = getKnownKATEType()

    override fun generateTo(block: LazyBlock, destination: DestinationStream) {
        // Generates nothing
    }

    override fun compareTo(model: KATEObject, other: KATEValue): Int {
        throw IllegalStateException("$this cannot be compared to $other")
    }

}