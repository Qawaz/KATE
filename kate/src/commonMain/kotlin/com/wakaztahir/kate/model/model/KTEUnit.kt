package com.wakaztahir.kate.model.model

import com.wakaztahir.kate.model.CodeGen
import com.wakaztahir.kate.model.LazyBlock
import com.wakaztahir.kate.model.ModelReference
import com.wakaztahir.kate.model.StringValue
import com.wakaztahir.kate.parser.stream.DestinationStream

object KTEUnit : ReferencedValue, CodeGen {

    override fun toString(): String = "KTEUnit"

    private val KTEUnitType = StringValue("unit")

    private val KTEUnitTypeFunction = object : KTEFunction() {
        override fun invoke(model: KTEObject, invokedOn: KTEValue, parameters: List<ReferencedValue>): KTEValue {
            return KTEUnitType
        }

        override fun toString(): String = this@KTEUnit.toString()
    }

    override fun getModelReference(reference: ModelReference): KTEValue? {
        return if (reference.name == "getType") {
            KTEUnitTypeFunction
        } else {
            null
        }
    }

    override fun generateTo(block: LazyBlock, destination: DestinationStream) {
        // Generates nothing
    }

    override fun compareTo(model: KTEObject, other: KTEValue): Int {
        throw IllegalStateException("$this cannot be compared to $other")
    }

}