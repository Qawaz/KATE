package com.wakaztahir.kate.model.model

import com.wakaztahir.kate.model.KATEType
import com.wakaztahir.kate.model.ModelReference
import com.wakaztahir.kate.parser.ArithmeticOperatorType
import com.wakaztahir.kate.runtime.KATEValueImplementation

abstract class KATEFunction(val returnedType: KATEType, val parameterTypes: List<KATEType>?) : KATEValue {

    constructor(returnedType: KATEType, vararg parameterTypes: KATEType) : this(
        returnedType = returnedType,
        parameterTypes = parameterTypes.ifEmpty { null }?.toList()
    )

    abstract fun invoke(
        model: KATEObject,
        invokedOn: KATEValue,
        explicitType: KATEType?,
        parameters: List<ReferencedOrDirectValue>
    ): ReferencedOrDirectValue

    override fun getModelReference(reference: ModelReference): KATEValue? {
        return KATEValueImplementation.propertyMap[reference.name]
    }

    override fun getKnownKATEType() = KATEType.Function(returnedType, parameterTypes)

    override fun toString(): String = getKnownKATEType().toString()

    override fun compareTo(other: KATEValue): Int {
        throw IllegalStateException("KATEFunction should be invoked first to get the value to compare with the other")
    }

    override fun operate(operator: ArithmeticOperatorType, other: KATEValue): KATEValue {
        throw IllegalStateException("KATEFunction should be invoked first to get the value to operate with the other")
    }

}