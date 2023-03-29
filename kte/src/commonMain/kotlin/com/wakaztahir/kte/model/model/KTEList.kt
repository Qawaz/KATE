package com.wakaztahir.kte.model.model

import com.wakaztahir.kte.model.*
import com.wakaztahir.kte.model.runtime.KTEListImplementation
import com.wakaztahir.kte.model.runtime.KTEMutableListImplementation
import com.wakaztahir.kte.parser.stream.DestinationStream
import kotlin.jvm.JvmInline

interface KTEList<T : KTEValue> : ReferencedValue {
    val collection: List<T>
}

interface KTEMutableList<T : KTEValue> : KTEList<T> {
    override val collection: MutableList<T>
}

@JvmInline
value class KTEListImpl<T : KTEValue>(override val collection: List<T>) : KTEList<T> {

    override fun getModelReference(reference: ModelReference): KTEValue? {
        if (reference is ModelReference.FunctionCall) {
            return KTEListImplementation.propertyMap[reference.name]
        } else {
            throw IllegalStateException("${reference.name} is not a property on list")
        }
    }

    override fun compareTo(model: KTEObject, other: KTEValue): Int {
        if (other is KTEList<*>) {
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

    override fun stringValue(indentationLevel: Int): String {
        return "${indentation(indentationLevel)}[" + collection.joinToString("\n") { it.stringValue(indentationLevel + 1) } + "]"
    }

}

@JvmInline
value class KTEMutableListImpl<T : KTEValue>(override val collection: MutableList<T>) : KTEMutableList<T> {

    override fun getModelReference(reference: ModelReference): KTEValue? {
        if (reference is ModelReference.FunctionCall) {
            return KTEMutableListImplementation.propertyMap[reference.name]
        } else {
            throw IllegalStateException("${reference.name} is not a property on list")
        }
    }

    override fun compareTo(model: KTEObject, other: KTEValue): Int {
        throw IllegalStateException("list $this cannot be compared to $other")
    }

    override fun toString(): String {
        return stringValue(0)
    }

    override fun stringValue(indentationLevel: Int): String {
        return "${indentation(indentationLevel)}[" + collection.joinToString("\n") { it.stringValue(indentationLevel + 1) } + "]"
    }


}