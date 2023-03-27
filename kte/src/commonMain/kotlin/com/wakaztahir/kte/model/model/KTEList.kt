package com.wakaztahir.kte.model.model

import com.wakaztahir.kte.model.*
import com.wakaztahir.kte.model.implentation.KTEListImplementation
import com.wakaztahir.kte.model.implentation.KTEMutableListImplementation
import com.wakaztahir.kte.parser.stream.DestinationStream

interface KTEList<T : KTEValue> : ReferencedValue {
    val collection: List<T>
}

interface KTEMutableList<T : KTEValue> : KTEList<T> {
    override val collection: MutableList<T>
}

open class KTEListImpl<T : KTEValue>(override val collection: List<T>) : List<T> by collection, KTEList<T> {

    override fun getModelReference(reference: ModelReference): KTEValue? {
        if (reference is ModelReference.FunctionCall) {
            return KTEListImplementation.propertyMap[reference.name]
        } else {
            throw IllegalStateException("${reference.name} is not a property on list")
        }
    }

    override fun generateTo(block: LazyBlock, destination: DestinationStream) {
        destination.writeList(this)
    }

    override fun toString(): String {
        return stringValue(0)
    }

    override fun stringValue(indentationLevel: Int): String {
        return "${indentation(indentationLevel)}[" + collection.joinToString("\n") { it.stringValue(indentationLevel + 1) } + "]"
    }

}

open class KTEMutableListImpl<T : KTEValue>(override val collection: MutableList<T>) : KTEMutableList<T>,
    KTEListImpl<T>(collection = collection) {

    override fun getModelReference(reference: ModelReference): KTEValue? {
        if (reference is ModelReference.FunctionCall) {
            return KTEMutableListImplementation.propertyMap[reference.name]
        } else {
            throw IllegalStateException("${reference.name} is not a property on list")
        }
    }

}