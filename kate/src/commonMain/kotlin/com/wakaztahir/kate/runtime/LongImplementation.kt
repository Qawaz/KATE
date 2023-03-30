package com.wakaztahir.kate.runtime

import com.wakaztahir.kate.model.DoubleValue
import com.wakaztahir.kate.model.IntValue
import com.wakaztahir.kate.model.LongValue
import com.wakaztahir.kate.model.StringValue
import com.wakaztahir.kate.model.model.KTEFunction
import com.wakaztahir.kate.model.model.KTEObject
import com.wakaztahir.kate.model.model.KTEValue
import com.wakaztahir.kate.model.model.ReferencedValue

object LongImplementation {

    val propertyMap by lazy { hashMapOf<String, KTEValue>().apply { putObjectFunctions() } }

    private fun HashMap<String, KTEValue>.putObjectFunctions() {
        put("getType", object : KTEFunction() {
            override fun invoke(model: KTEObject, invokedOn: KTEValue, parameters: List<ReferencedValue>): KTEValue {
                return StringValue("long")
            }

            override fun toString(): String = "getType() : string"
        })
        put("toString", object : KTEFunction() {
            override fun invoke(model: KTEObject, invokedOn: KTEValue, parameters: List<ReferencedValue>): KTEValue {
                val intVal = invokedOn.let { it as? LongValue }?.value
                require(intVal != null) { "long value is null" }
                return StringValue(intVal.toString())
            }

            override fun toString(): String = "toString() : string"
        })
        put("toInt", object : KTEFunction() {
            override fun invoke(model: KTEObject, invokedOn: KTEValue, parameters: List<ReferencedValue>): KTEValue {
                val intVal = invokedOn.let { it as? LongValue }?.value
                require(intVal != null) { "long value is null" }
                return IntValue(intVal.toInt())
            }

            override fun toString(): String = "toInt() : int"
        })
        put("toDouble", object : KTEFunction() {
            override fun invoke(model: KTEObject, invokedOn: KTEValue, parameters: List<ReferencedValue>): KTEValue {
                val intVal = invokedOn.let { it as? LongValue }?.value
                require(intVal != null) { "long value is null" }
                return DoubleValue(intVal.toDouble())
            }

            override fun toString(): String = "toDouble() : double"
        })
    }

}