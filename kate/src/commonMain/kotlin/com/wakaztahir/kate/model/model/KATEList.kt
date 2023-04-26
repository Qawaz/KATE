package com.wakaztahir.kate.model.model

import com.wakaztahir.kate.model.*
import com.wakaztahir.kate.parser.ArithmeticOperatorType
import com.wakaztahir.kate.runtime.KATEListImplementation
import com.wakaztahir.kate.runtime.KATEMutableListImplementation

interface KATEList<T : KATEValue> : KATEValue {

    val itemType: KATEType

    val collection: List<T>

    fun getExplicitType(index: Int): KATEType?

    fun setExplicitType(index: Int, type: KATEType)

    override fun getKnownKATEType(): KATEType

    override fun operate(operator: ArithmeticOperatorType, other: KATEValue): KATEValue {
        TODO("Not yet implemented")
    }

    override fun compareTo(other: KATEValue): Int {
        if (other is KATEList<*>) {
            if (this.collection.isEmpty() && other.collection.isEmpty()) return 0
            if (this.collection.size != other.collection.size) return -1
            if (this.collection != other.collection) return -1
            return 0
        }
        return -1
    }

}

interface KATEMutableList<T : KATEValue> : KATEList<T> {
    override val collection: MutableList<T>
}

open class KATEListImpl<T : KATEValue>(override val collection: List<T>, override val itemType: KATEType) :
    KATEList<T> {

    private val itemTypes by lazy { hashMapOf<Int, KATEType>() }

    override fun getKnownKATEType(): KATEType = KATEType.List(itemType)

    override fun getExplicitType(index: Int): KATEType? = itemTypes[index]

    override fun setExplicitType(index: Int, type: KATEType) {
        itemTypes[index] = type
    }

    override fun getModelReference(reference: ModelReference): KATEValue? {
        if (reference is ModelReference.FunctionCall) {
            return KATEListImplementation.propertyMap[reference.name]
        } else {
            throw IllegalStateException("${reference.name} is not a property on list")
        }
    }

    override fun toString(): String {
        return collection.joinToString(",")
    }

}

class KATEMutableListImpl<T : KATEValue>(collection: MutableList<T>, override val itemType: KATEType) :
    KATEListImpl<T>(collection, itemType) {

    override fun getModelReference(reference: ModelReference): KATEValue? {
        if (reference is ModelReference.FunctionCall) {
            return KATEMutableListImplementation.propertyMap[reference.name]
        } else {
            throw IllegalStateException("${reference.name} is not a property on list")
        }
    }

    override fun getKnownKATEType(): KATEType = KATEType.MutableList(itemType)

}