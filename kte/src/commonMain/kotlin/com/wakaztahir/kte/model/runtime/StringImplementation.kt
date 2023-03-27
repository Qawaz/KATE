package com.wakaztahir.kte.model.runtime

import com.wakaztahir.kte.model.CharValue
import com.wakaztahir.kte.model.DoubleValue
import com.wakaztahir.kte.model.IntValue
import com.wakaztahir.kte.model.StringValue
import com.wakaztahir.kte.model.model.*

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
        put("toInt", object : KTEFunction() {
            override fun invoke(model: KTEObject, parameters: List<ReferencedValue>): KTEValue {
                val string = invokedOn?.let { it as? StringValue }?.value
                require(string != null) { "string value is null" }
                return string.toIntOrNull()?.let { IntValue(it) } ?: KTEUnit
            }

            override fun toString(): String = "toInt() : Int"
        })
        put("toDouble", object : KTEFunction() {
            override fun invoke(model: KTEObject, parameters: List<ReferencedValue>): KTEValue {
                val string = invokedOn?.let { it as? StringValue }?.value
                require(string != null) { "string value is null" }
                return string.toDoubleOrNull()?.let { DoubleValue(it) } ?: KTEUnit
            }

            override fun toString(): String = "toDouble() : Double"
        })
    }

}