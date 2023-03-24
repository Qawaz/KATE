package com.wakaztahir.kte.model.model

import com.wakaztahir.kte.dsl.ModelValue
import com.wakaztahir.kte.model.*
import com.wakaztahir.kte.parser.stream.DestinationStream
import com.wakaztahir.kte.parser.stream.LanguageDestination
import com.wakaztahir.kte.parser.stream.SourceStream

class ModelListImpl<T : KTEValue>(override val objectName: String, val collection: List<T>) : List<T> by collection,
    KTEList<T>() {

    private val props: MutableMap<String, KTEValue> by lazy {
        hashMapOf<String, KTEValue>().apply {
            put("get", object : KTEFunction() {
                override fun invoke(model: KTEObject, parameters: List<ReferencedValue>): ModelValue {
                    require(parameters.size == 1) {
                        "Unknown parameters to get function"
                    }
                    return ModelValue(collection[parameters[0].asPrimitive(model).value as Int])
                }

                override fun toString(): String = "get(number) : KTEValue"

            })
            put("size", object : ReferencedValue {
                override fun asPrimitive(model: KTEObject): PrimitiveValue<*> {
                    return IntValue(collection.size)
                }

                override fun writeTo(model: KTEObject, destination: LanguageDestination) {
                    destination.write(IntValue(collection.size))
                }

                override fun stringValue(indentationLevel: Int): String = toString()
                override fun toString(): String = "size : Int"
            })
            put("contains", object : KTEFunction() {
                override fun invoke(model: KTEObject, parameters: List<ReferencedValue>): ModelValue {
                    @Suppress("UNCHECKED_CAST")
                    return ModelValue(collection.containsAll(parameters as List<T>))
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

    override fun writeTo(model: KTEObject, destination: LanguageDestination) {
        destination.write(this)
    }

    override fun toString(): String {
        return stringValue(0)
    }

    override fun stringValue(indentationLevel: Int): String {
        return "${indentation(indentationLevel)}[" + collection.joinToString("\n") { it.stringValue(indentationLevel + 1) } + "]"
    }

}