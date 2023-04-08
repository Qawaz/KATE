package com.wakaztahir.kate.runtime

import com.wakaztahir.kate.model.BooleanValue
import com.wakaztahir.kate.model.StringValue
import com.wakaztahir.kate.model.model.*

object KTEMutableListImplementation {

    val propertyMap by lazy { hashMapOf<String, KATEValue>().apply { putObjectFunctions() } }

    private fun HashMap<String, KATEValue>.putObjectFunctions() {
        with(KATEValueImplementation){ putObjectFunctions() }
        with(KTEListImplementation) { putObjectFunctions() }
        put("getType", object : KATEFunction() {
            override fun invoke(model: KATEObject, invokedOn: KATEValue, parameters: List<ReferencedValue>): KATEValue {
                return StringValue("mutable_list")
            }
            override fun toString(): String = "getType() : string"
        })
        put("add", object : KATEFunction() {
            override fun invoke(model: KATEObject, invokedOn: KATEValue, parameters: List<ReferencedValue>): KATEValue {
                require(parameters.size == 1) {
                    "mutable_list.add(e : Element) expects a single parameter instead of ${parameters.size}"
                }
                return BooleanValue(invokedOn.asNullableMutableList(model)!!.collection.add(parameters[0]))
            }

            override fun toString(): String = "add(e : Element) : KTEValue"
        })
        put("addAt", object : KATEFunction() {
            override fun invoke(model: KATEObject, invokedOn: KATEValue, parameters: List<ReferencedValue>): KATEValue {
                val index = parameters.getOrNull(0)?.asNullablePrimitive(model)?.value as? Int
                require(parameters.size == 2 && index != null) {
                    "mutable_list.addAt(index : Int,e : Element) expects two parameters instead of ${parameters.size}"
                }
                invokedOn.asNullableMutableList(model)!!.collection.add(index, parameters[1])
                return KATEUnit
            }

            override fun toString(): String = "addAt(index : Int,e : Element) : Boolean"
        })
        put("remove", object : KATEFunction() {
            override fun invoke(model: KATEObject, invokedOn: KATEValue, parameters: List<ReferencedValue>): KATEValue {
                require(parameters.size == 1) {
                    "mutable_list.remove(e : Element) expects a single parameter instead of ${parameters.size}"
                }
                return BooleanValue(invokedOn.asNullableMutableList(model)!!.collection.remove(parameters[0]))
            }

            override fun toString(): String = "remove(e : Element) : Boolean"
        })
        put("removeAt", object : KATEFunction() {
            override fun invoke(model: KATEObject, invokedOn: KATEValue, parameters: List<ReferencedValue>): KATEValue {
                val index = parameters.getOrNull(0)?.asNullablePrimitive(model)?.value as? Int
                require(index != null) {
                    "mutable_list.removeAt(index : Int) expects single parameter instead of ${parameters.size}"
                }
                return invokedOn.asNullableMutableList(model)!!.collection.removeAt(index)
            }

            override fun toString(): String = "removeAt(index : Int) : Boolean"
        })
    }

}