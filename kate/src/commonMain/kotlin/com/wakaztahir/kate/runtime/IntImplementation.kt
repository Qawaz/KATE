package com.wakaztahir.kate.runtime

import com.wakaztahir.kate.model.DoubleValue
import com.wakaztahir.kate.model.IntValue
import com.wakaztahir.kate.model.LongValue
import com.wakaztahir.kate.model.StringValue
import com.wakaztahir.kate.model.model.*

object IntImplementation {

    val propertyMap by lazy { hashMapOf<String, KTEValue>().apply { putObjectFunctions() } }

    private fun HashMap<String, KTEValue>.putObjectFunctions() {
        put("getType", object : KTEFunction() {
            override fun invoke(model: KTEObject, invokedOn: KTEValue, parameters: List<ReferencedValue>): KTEValue {
                return StringValue("int")
            }

            override fun toString(): String = "getType() : string"
        })
        put("toString", object : KTEFunction() {
            override fun invoke(model: KTEObject, invokedOn: KTEValue, parameters: List<ReferencedValue>): KTEValue {
                val intVal = invokedOn.let { it as? IntValue }?.value
                require(intVal != null) { "int value is null" }
                return StringValue(intVal.toString())
            }

            override fun toString(): String = "toString() : string"
        })
        put("toDouble", object : KTEFunction() {
            override fun invoke(model: KTEObject, invokedOn: KTEValue, parameters: List<ReferencedValue>): KTEValue {
                val intVal = invokedOn.let { it as? IntValue }?.value
                require(intVal != null) { "int value is null" }
                return DoubleValue(intVal.toDouble())
            }

            override fun toString(): String = "toDouble() : double"
        })
        put("toLong", object : KTEFunction() {
            override fun invoke(model: KTEObject, invokedOn: KTEValue, parameters: List<ReferencedValue>): KTEValue {
                val intVal = invokedOn.let { it as? IntValue }?.value
                require(intVal != null) { "int value is null" }
                return LongValue(intVal.toLong())
            }

            override fun toString(): String = "toLong() : long"
        })
    }

}