package com.wakaztahir.kate.runtime

import com.wakaztahir.kate.model.DoubleValue
import com.wakaztahir.kate.model.IntValue
import com.wakaztahir.kate.model.LongValue
import com.wakaztahir.kate.model.StringValue
import com.wakaztahir.kate.model.model.*
import com.wakaztahir.kate.runtime.KTEListImplementation.putObjectFunctions

object DoubleImplementation {

    val propertyMap by lazy { hashMapOf<String, KATEValue>().apply { putObjectFunctions() } }

    private fun HashMap<String, KATEValue>.putObjectFunctions() {
        with(KATEValueImplementation){ putObjectFunctions() }
        put("getType", object : KATEFunction() {
            override fun invoke(model: KATEObject, invokedOn: KATEValue, parameters: List<ReferencedValue>): KATEValue {
                return StringValue("double")
            }

            override fun toString(): String = "getType() : string"
        })
        put("toInt", object : KATEFunction() {
            override fun invoke(model: KATEObject, invokedOn: KATEValue, parameters: List<ReferencedValue>): KATEValue {
                val intVal = invokedOn.let { it as? DoubleValue }?.value
                require(intVal != null) { "double value is null" }
                return IntValue(intVal.toInt())
            }

            override fun toString(): String = "toInt() : int"
        })
        put("toLong", object : KATEFunction() {
            override fun invoke(model: KATEObject, invokedOn: KATEValue, parameters: List<ReferencedValue>): KATEValue {
                val intVal = invokedOn.let { it as? DoubleValue }?.value
                require(intVal != null) { "double value is null" }
                return LongValue(intVal.toLong())
            }

            override fun toString(): String = "toLong() : long"
        })
    }

}