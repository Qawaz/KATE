package com.wakaztahir.kte.model.runtime

import com.wakaztahir.kte.model.CharValue
import com.wakaztahir.kte.model.IntValue
import com.wakaztahir.kte.model.StringValue
import com.wakaztahir.kte.model.model.KTEFunction
import com.wakaztahir.kte.model.model.KTEObject
import com.wakaztahir.kte.model.model.KTEValue
import com.wakaztahir.kte.model.model.ReferencedValue

object StringImplementation {

    val propertyMap by lazy { hashMapOf<String, KTEValue>().apply { putObjectFunctions() } }

    private fun HashMap<String, KTEValue>.putObjectFunctions() {
        put("get", object : KTEFunction() {
            override fun invoke(model: KTEObject, parameters: List<ReferencedValue>): KTEValue {
                val string = invokedOn?.let { it as? StringValue }?.value
                val index = parameters.getOrNull(0)?.asNullablePrimitive(model)?.value?.let { it as? Int }
                require(string != null) { "string value is null" }
                require(index != null) { "String.get(Int) expects a single parameter to get the value of string" }
                return CharValue(string[index])
            }

            override fun toString(): String = "get(Int) : Char"
        })
        put("size", object : KTEFunction() {
            override fun invoke(model: KTEObject, parameters: List<ReferencedValue>): KTEValue {
                val string = invokedOn?.let { it as? StringValue }?.value
                require(string != null) { "string value is null" }
                return IntValue(string.length)
            }

            override fun toString(): String = "size() : Int"
        })
    }

}