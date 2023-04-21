package com.wakaztahir.kate.model.model

import com.wakaztahir.kate.model.CodeGen
import com.wakaztahir.kate.model.LazyBlock
import com.wakaztahir.kate.model.ModelReference
import com.wakaztahir.kate.model.StringValue
import com.wakaztahir.kate.parser.stream.DestinationStream

object KATEUnit : ReferencedValue, CodeGen {

    override fun toString(): String = "KATEUnit"

    private val KATEUnitType = StringValue("unit")

    override fun getKateType(model: KATEObject): String = KATEUnitType.value

    private val KTEUnitTypeFunction = object : KATEFunction() {
        override fun invoke(
            model: KATEObject,
            path: List<ModelReference>,
            pathIndex: Int,
            invokedOn: KATEValue,
            parameters: List<ReferencedValue>
        ): KATEValue = KATEUnitType
        override fun toString(): String = this@KATEUnit.toString()
    }

    override fun getModelReference(reference: ModelReference): KATEValue? {
        return if (reference.name == "getType") {
            KTEUnitTypeFunction
        } else {
            null
        }
    }

    override fun generateTo(block: LazyBlock, destination: DestinationStream) {
        // Generates nothing
    }

    override fun compareTo(model: KATEObject, other: KATEValue): Int {
        throw IllegalStateException("$this cannot be compared to $other")
    }

}