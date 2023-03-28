package com.wakaztahir.kte.model.runtime

import com.wakaztahir.kte.model.BooleanValue
import com.wakaztahir.kte.model.model.*

object KTEMutableListImplementation {

    val propertyMap by lazy { hashMapOf<String, KTEValue>().apply { putObjectFunctions() } }

    private fun HashMap<String, KTEValue>.putObjectFunctions() {
        with(KTEListImplementation) { putObjectFunctions() }
        put("add", object : KTEFunction() {
            override fun invoke(model: KTEObject, invokedOn: KTEValue, parameters: List<ReferencedValue>): KTEValue {
                require(parameters.size == 1) {
                    "mutable_list.add(e : Element) expects a single parameter instead of ${parameters.size}"
                }
                return BooleanValue(invokedOn.asNullableMutableList(model)!!.collection.add(parameters[0]))
            }

            override fun toString(): String = "add(e : Element) : KTEValue"
        })
        put("addAt", object : KTEFunction() {
            override fun invoke(model: KTEObject, invokedOn: KTEValue, parameters: List<ReferencedValue>): KTEValue {
                val index = parameters.getOrNull(0)?.asNullablePrimitive(model)?.value as? Int
                require(parameters.size == 2 && index != null) {
                    "mutable_list.addAt(index : Int,e : Element) expects two parameters instead of ${parameters.size}"
                }
                invokedOn.asNullableMutableList(model)!!.collection.add(index, parameters[1])
                return KTEUnit
            }

            override fun toString(): String = "addAt(index : Int,e : Element) : Boolean"
        })
        put("remove", object : KTEFunction() {
            override fun invoke(model: KTEObject, invokedOn: KTEValue, parameters: List<ReferencedValue>): KTEValue {
                require(parameters.size == 1) {
                    "mutable_list.remove(e : Element) expects a single parameter instead of ${parameters.size}"
                }
                return BooleanValue(invokedOn.asNullableMutableList(model)!!.collection.remove(parameters[0]))
            }

            override fun toString(): String = "remove(e : Element) : Boolean"
        })
        put("removeAt", object : KTEFunction() {
            override fun invoke(model: KTEObject, invokedOn: KTEValue, parameters: List<ReferencedValue>): KTEValue {
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