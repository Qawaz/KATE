package com.wakaztahir.kte.model.model

import com.wakaztahir.kte.model.*
import com.wakaztahir.kte.parser.stream.DestinationStream

interface KTEList<T : KTEValue> : KTEObject {
    val collection: List<T>
}

interface MutableKTEList<T : KTEValue> : KTEList<T> {
    override val collection: MutableList<T>
}

open class KTEListImpl<T : KTEValue>(
    override val objectName: String,
    override val collection: List<T>
) : List<T> by collection, KTEList<T> {

    override val contained: MutableMap<String, KTEValue> by lazy {
        hashMapOf<String, KTEValue>().apply { putImmutableListFunctions() }
    }

    open fun HashMap<String, KTEValue>.putImmutableListFunctions() {
        put("get", object : KTEFunction() {
            override fun invoke(model: KTEObject, parameters: List<KTEValue>): KTEValue {
                val index = parameters.getOrNull(0)?.asNullablePrimitive(model)?.value as? Int
                require(index != null) {
                    "list.get(int) expects a single Int parameter instead of ${parameters.size}"
                }
                return collection[index]
            }

            override fun toString(): String = "get(number) : KTEValue"

        })
        put("size", object : KTEFunction() {
            override fun invoke(model: KTEObject, parameters: List<KTEValue>): KTEValue {
                return IntValue(collection.size)
            }

            override fun toString(): String = "size() : Int"
        })
        put("contains", object : KTEFunction() {
            override fun invoke(model: KTEObject, parameters: List<KTEValue>): KTEValue {
                @Suppress("UNCHECKED_CAST")
                return BooleanValue(collection.containsAll(parameters as List<T>))
            }

            override fun toString(): String = "contains(parameter) : Boolean"

        })
    }

    override fun getModelReference(reference: ModelReference): KTEValue? {
        if (reference is ModelReference.FunctionCall) {
            return contained[reference.name]
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

open class KTEMutableListImpl<T : KTEValue>(
    override val objectName: String,
    override val collection: MutableList<T>
) : MutableKTEList<T>, KTEListImpl<T>(objectName, collection = collection) {

    override fun HashMap<String, KTEValue>.putImmutableListFunctions() {
        put("add", object : KTEFunction() {
            override fun invoke(model: KTEObject, parameters: List<KTEValue>): KTEValue {
                require(parameters.size == 1) {
                    "mutable_list.add(e : Element) expects a single parameter instead of ${parameters.size}"
                }
                @Suppress("UNCHECKED_CAST")
                return BooleanValue(collection.add(parameters[0] as T))
            }

            override fun toString(): String = "add(e : Element) : KTEValue"
        })
        put("addAt", object : KTEFunction() {
            override fun invoke(model: KTEObject, parameters: List<KTEValue>): KTEValue {
                val index = parameters.getOrNull(0)?.asNullablePrimitive(model)?.value as? Int
                require(parameters.size == 2 && index != null) {
                    "mutable_list.addAt(index : Int,e : Element) expects two parameters instead of ${parameters.size}"
                }
                @Suppress("UNCHECKED_CAST")
                collection.add(index, parameters[1] as T)
                return KTEUnit
            }

            override fun toString(): String = "addAt(index : Int,e : Element) : Boolean"
        })
        put("remove", object : KTEFunction() {
            override fun invoke(model: KTEObject, parameters: List<KTEValue>): KTEValue {
                require(parameters.size == 1) {
                    "mutable_list.remove(e : Element) expects a single parameter instead of ${parameters.size}"
                }
                @Suppress("UNCHECKED_CAST")
                return BooleanValue(collection.remove(parameters[0] as T))
            }

            override fun toString(): String = "remove(e : Element) : Boolean"
        })
        put("removeAt", object : KTEFunction() {
            override fun invoke(model: KTEObject, parameters: List<KTEValue>): KTEValue {
                val index = parameters.getOrNull(0)?.asNullablePrimitive(model)?.value as? Int
                require(index != null) {
                    "mutable_list.removeAt(index : Int) expects single parameter instead of ${parameters.size}"
                }
                return collection.removeAt(index)
            }

            override fun toString(): String = "removeAt(index : Int) : Boolean"
        })
    }
}