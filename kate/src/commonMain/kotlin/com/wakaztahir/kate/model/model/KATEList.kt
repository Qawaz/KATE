package com.wakaztahir.kate.model.model

import com.wakaztahir.kate.model.*
import com.wakaztahir.kate.runtime.KATEListImplementation
import com.wakaztahir.kate.runtime.KATEMutableListImplementation
import kotlin.jvm.JvmInline

interface KATEList<T : KATEValue> : ReferencedValue {
    val collection: List<T>
}

interface KATEMutableList<T : KATEValue> : KATEList<T> {
    override val collection: MutableList<T>
}

@JvmInline
value class KATEListImpl<T : KATEValue>(override val collection: List<T>) : KATEList<T> {

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

@JvmInline
value class KATEMutableListImpl<T : KATEValue>(override val collection: MutableList<T>) : KATEMutableList<T> {

    override fun getModelReference(reference: ModelReference): KATEValue? {
        if (reference is ModelReference.FunctionCall) {
            return KATEMutableListImplementation.propertyMap[reference.name]
        } else {
            throw IllegalStateException("${reference.name} is not a property on list")
        }
    }

    override fun compareTo(model: KATEObject, other: KATEValue): Int {
        throw IllegalStateException("list $this cannot be compared to $other")
    }

    override fun toString(): String {
        return collection.joinToString(",")
    }

}