package com.wakaztahir.kte.runtime

import com.wakaztahir.kte.model.DoubleValue
import com.wakaztahir.kte.model.IntValue
import com.wakaztahir.kte.model.StringValue
import com.wakaztahir.kte.model.model.*

object DoubleImplementation {

    val propertyMap by lazy { hashMapOf<String, KTEValue>().apply { putObjectFunctions() } }

    private fun HashMap<String, KTEValue>.putObjectFunctions() {
        put("getType", object : KTEFunction() {
            override fun invoke(model: KTEObject, invokedOn: KTEValue, parameters: List<ReferencedValue>): KTEValue {
                return StringValue("double")
            }
            override fun toString(): String = "getType() : string"
        })
        put("toString", object : KTEFunction() {
            override fun invoke(model: KTEObject, invokedOn: KTEValue, parameters: List<ReferencedValue>): KTEValue {
                val intVal = invokedOn.let { it as? DoubleValue }?.value
                require(intVal != null) { "double value is null" }
                return StringValue(intVal.toString())
            }

            override fun toString(): String = "toString() : String"
        })
        put("toInt", object : KTEFunction() {
            override fun invoke(model: KTEObject, invokedOn: KTEValue, parameters: List<ReferencedValue>): KTEValue {
                val intVal = invokedOn.let { it as? DoubleValue }?.value
                require(intVal != null) { "double value is null" }
                return IntValue(intVal.toInt())
            }

            override fun toString(): String = "toInt() : Int"
        })
    }

}