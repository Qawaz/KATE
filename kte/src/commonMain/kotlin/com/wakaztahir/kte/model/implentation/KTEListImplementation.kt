package com.wakaztahir.kte.model.implentation

import com.wakaztahir.kte.model.BooleanValue
import com.wakaztahir.kte.model.IntValue
import com.wakaztahir.kte.model.model.KTEFunction
import com.wakaztahir.kte.model.model.KTEObject
import com.wakaztahir.kte.model.model.KTEValue
import com.wakaztahir.kte.model.model.ReferencedValue

object KTEListImplementation {

    val propertyMap by lazy { hashMapOf<String, KTEValue>().apply { putObjectFunctions() } }

    fun HashMap<String, KTEValue>.putObjectFunctions() {
        put("get", object : KTEFunction() {
            override fun invoke(model: KTEObject, parameters: List<ReferencedValue>): KTEValue {
                val index = parameters.getOrNull(0)?.asNullablePrimitive(model)?.value as? Int
                require(invokedOn != null && index != null) {
                    "list.get(int) expects a single Int parameter instead of ${parameters.size}"
                }
                return (invokedOn!!.asNullableList(model)!!.collection)[index]
            }

            override fun toString(): String = "get(number) : KTEValue"

        })
        put("size", object : KTEFunction() {
            override fun invoke(model: KTEObject, parameters: List<ReferencedValue>): KTEValue {
                return IntValue(invokedOn!!.asNullableList(model)!!.collection.size)
            }

            override fun toString(): String = "size() : Int"
        })
        put("contains", object : KTEFunction() {
            override fun invoke(model: KTEObject, parameters: List<ReferencedValue>): KTEValue {
                return BooleanValue(invokedOn!!.asNullableList(model)!!.collection.containsAll(parameters))
            }
            override fun toString(): String = "contains(parameter) : Boolean"

        })
    }

}

