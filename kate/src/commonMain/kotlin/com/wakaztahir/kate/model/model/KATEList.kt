package com.wakaztahir.kate.model.model

import com.wakaztahir.kate.model.*
import com.wakaztahir.kate.runtime.KATEListImplementation
import com.wakaztahir.kate.runtime.KATEMutableListImplementation

interface KATEList<T : KATEValue> : KATEValue {

    val itemType : KATEType

    val collection: List<T>

    override fun getKnownKATEType(): KATEType

    override fun getKATEType(model: KATEObject): KATEType = getKnownKATEType()

}

interface KATEMutableList<T : KATEValue> : KATEList<T> {
    override val collection: MutableList<T>
}

class KATEListImpl<T : KATEValue>(override val collection: List<T>,override val itemType : KATEType) : KATEList<T> {

    override fun getKnownKATEType(): KATEType = KATEType.List(itemType)

    override fun getModelReference(reference: ModelReference): KATEValue? {
        if (reference is ModelReference.FunctionCall) {
            return KATEListImplementation.propertyMap[reference.name]
        } else {
            throw IllegalStateException("${reference.name} is not a property on list")
        }
    }

    override fun compareTo(model: KATEObject, other: KATEValue): Int {
        if (other is KATEList<*>) {
            if (this.collection.isEmpty() && other.collection.isEmpty()) return 0
            if (this.collection.size != other.collection.size) return -1
            if (this.collection != other.collection) return -1
            return 0
        }
        return -1
    }

    override fun toString(): String {
        return collection.joinToString(",")
    }

}

class KATEMutableListImpl<T : KATEValue>(override val collection: MutableList<T>,override val itemType: KATEType) : KATEMutableList<T> {

    override fun getModelReference(reference: ModelReference): KATEValue? {
        if (reference is ModelReference.FunctionCall) {
            return KATEMutableListImplementation.propertyMap[reference.name]
        } else {
            throw IllegalStateException("${reference.name} is not a property on list")
        }
    }

    override fun getKnownKATEType(): KATEType = KATEType.MutableList(itemType)

    override fun compareTo(model: KATEObject, other: KATEValue): Int {
        throw IllegalStateException("list $this cannot be compared to $other")
    }

    override fun toString(): String {
        return collection.joinToString(",")
    }

}