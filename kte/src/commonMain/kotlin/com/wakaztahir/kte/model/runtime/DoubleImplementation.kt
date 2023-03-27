package com.wakaztahir.kte.model.runtime

import com.wakaztahir.kte.model.DoubleValue
import com.wakaztahir.kte.model.IntValue
import com.wakaztahir.kte.model.StringValue
import com.wakaztahir.kte.model.model.KTEFunction
import com.wakaztahir.kte.model.model.KTEObject
import com.wakaztahir.kte.model.model.KTEValue
import com.wakaztahir.kte.model.model.ReferencedValue

object DoubleImplementation {

    val propertyMap by lazy { hashMapOf<String, KTEValue>().apply { putObjectFunctions() } }

    private fun HashMap<String, KTEValue>.putObjectFunctions() {
        put("toString", object : KTEFunction() {
            override fun invoke(model: KTEObject, parameters: List<ReferencedValue>): KTEValue {
                val intVal = invokedOn?.let { it as? DoubleValue }?.value
                require(intVal != null) { "double value is null" }
                return StringValue(intVal.toString())
            }

            override fun toString(): String = "toString() : String"
        })
        put("toInt", object : KTEFunction() {
            override fun invoke(model: KTEObject, parameters: List<ReferencedValue>): KTEValue {
                val intVal = invokedOn?.let { it as? DoubleValue }?.value
                require(intVal != null) { "double value is null" }
                return IntValue(intVal.toInt())
            }

            override fun toString(): String = "toInt() : Int"
        })
    }

}