package com.wakaztahir.kate.model.model

import com.wakaztahir.kate.model.*
import com.wakaztahir.kate.parser.ArithmeticOperatorType
import com.wakaztahir.kate.parser.stream.DestinationStream
import com.wakaztahir.kate.runtime.KATEValueImplementation
import com.wakaztahir.kate.tokenizer.NodeTokenizer

object KATEUnit : KATEValue, CodeGen {

    override fun <T> selectNode(tokenizer: NodeTokenizer<T>): T = tokenizer.kateUnit

    override fun getKotlinValue(): Any = Unit

    override fun toString(): String = "KATEUnit"

    override fun getModelReference(reference: ModelReference): KATEValue? {
        return KATEValueImplementation.propertyMap[reference.name]
    }

    override fun getKnownKATEType(): KATEType = KATEType.Unit

    override fun generateTo(block: LazyBlock, destination: DestinationStream) {
        // Generates nothing
    }

    override fun operate(operator: ArithmeticOperatorType, other: KATEValue): KATEValue {
        throw IllegalStateException("$this cannot be ${operator.char} to $other")
    }

    override fun compareTo(other: KATEValue): Int {
        throw IllegalStateException("$this cannot be compared to $other")
    }

}