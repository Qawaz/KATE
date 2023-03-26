package com.wakaztahir.kte.model.model

import com.wakaztahir.kte.model.*
import com.wakaztahir.kte.parser.stream.DestinationStream

class ModelListImpl<T : KTEValue>(override val objectName: String, val collection: List<T>) : List<T> by collection,
    KTEList<T>() {

    private val props: MutableMap<String, KTEValue> by lazy {
        hashMapOf<String, KTEValue>().apply {
            put("get", object : KTEFunction() {
                override fun invoke(model: KTEObject, parameters: List<ReferencedValue>): KTEValue {
                    require(parameters.size == 1) {
                        "mutable_list.get(int) expects a single parameter instead of ${parameters.size}"
                    }
                    return collection[parameters[0].asPrimitive(model).value as Int]
                }

                override fun toString(): String = "get(number) : KTEValue"

            })
            put("size", object : KTEFunction() {
                override fun invoke(model: KTEObject, parameters: List<ReferencedValue>): KTEValue {
                    return IntValue(collection.size)
                }

                override fun toString(): String = "size() : Int"
            })
            put("contains", object : KTEFunction() {
                override fun invoke(model: KTEObject, parameters: List<ReferencedValue>): KTEValue {
                    @Suppress("UNCHECKED_CAST")
                    return BooleanValue(collection.containsAll(parameters as List<T>))
                }

                override fun toString(): String = "contains(parameter) : Boolean"

            })
        }
    }

    override val contained: Map<String, KTEValue>
        get() = props

    override fun getModelReference(reference: ModelReference): KTEValue? {
        return props[reference.name]
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