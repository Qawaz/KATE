package com.wakaztahir.kate.runtime

import com.wakaztahir.kate.model.BooleanValue
import com.wakaztahir.kate.model.KATEType
import com.wakaztahir.kate.model.model.*

object KATEMutableListImplementation {

    val propertyMap by lazy { hashMapOf<String, KATEValue>().apply { putObjectFunctions() } }

    private fun HashMap<String, KATEValue>.putObjectFunctions() {
        with(KATEValueImplementation){ putObjectFunctions() }
        with(KATEListImplementation) { putObjectFunctions() }
        put("add", object : KATEFunction(KATEType.Boolean,KATEType.Any) {
            override fun invoke(
                model: KATEObject,
                invokedOn: KATEValue,
                explicitType: KATEType?,
                parameters: List<ReferencedOrDirectValue>
            ): ReferencedOrDirectValue {
                require(parameters.size == 1) {
                    "mutable_list.add(e : Element) expects a single parameter instead of ${parameters.size}"
                }
                return BooleanValue(invokedOn.asNullableMutableList(model)!!.collection.add(parameters[0].getKATEValue(model)))
            }

            override fun toString(): String = "add(e : Element) : KTEValue"
        })
        put("addAt", object : KATEFunction(KATEType.Unit,KATEType.Int,KATEType.Any) {
            override fun invoke(
                model: KATEObject,
                invokedOn: KATEValue,
                explicitType: KATEType?,
                parameters: List<ReferencedOrDirectValue>
            ): ReferencedOrDirectValue {
                val index = parameters.getOrNull(0)?.asNullablePrimitive(model)?.value as? Int
                require(parameters.size == 2 && index != null) {
                    "mutable_list.addAt(index : Int,e : Element) expects two parameters instead of ${parameters.size}"
                }
                invokedOn.asNullableMutableList(model)!!.collection.add(index, parameters[1].getKATEValue(model))
                return KATEUnit
            }

            override fun toString(): String = "addAt(index : Int,e : Element) : Boolean"
        })
        put("remove", object : KATEFunction(KATEType.Boolean,KATEType.Any) {
            override fun invoke(
                model: KATEObject,
                invokedOn: KATEValue,
                explicitType: KATEType?,
                parameters: List<ReferencedOrDirectValue>
            ): ReferencedOrDirectValue {
                require(parameters.size == 1) {
                    "mutable_list.remove(e : Element) expects a single parameter instead of ${parameters.size}"
                }
                return BooleanValue(invokedOn.asNullableMutableList(model)!!.collection.remove(parameters[0]))
            }

            override fun toString(): String = "remove(e : Element) : Boolean"
        })
        put("removeAt", object : KATEFunction(KATEType.Any,KATEType.Int) {
            override fun invoke(
                model: KATEObject,
                invokedOn: KATEValue,
                explicitType: KATEType?,
                parameters: List<ReferencedOrDirectValue>
            ): ReferencedOrDirectValue {
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