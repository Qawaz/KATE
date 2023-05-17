package com.wakaztahir.kate.model

import com.wakaztahir.kate.model.model.KATEList
import com.wakaztahir.kate.model.model.KATEListImpl
import com.wakaztahir.kate.model.model.KATEValue
import com.wakaztahir.kate.parser.ArithmeticOperatorType
import com.wakaztahir.kate.runtime.KATEEnumImplementation

class KATEEnum(val values: KATEList<StringValue>) : KATEValue {

    constructor(values: List<String>) : this(KATEListImpl(values.map { StringValue(it) }, itemType = KATEType.String))

    override fun getModelReference(reference: ModelReference): KATEValue? {
        return KATEEnumImplementation.propertyMap[reference.name]
    }

    override fun getKnownKATEType(): KATEType = KATEType.Enum(values)

    override fun getKotlinValue(): Any = values

    override fun toString(): String = values.toString()

    override fun operate(operator: ArithmeticOperatorType, other: KATEValue): KATEValue {
        TODO("Not yet implemented")
    }

    override fun compareTo(other: KATEValue): Int {
        TODO("Not yet implemented")
    }
}