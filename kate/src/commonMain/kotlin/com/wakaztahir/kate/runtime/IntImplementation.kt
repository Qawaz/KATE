package com.wakaztahir.kate.runtime

import com.wakaztahir.kate.model.*
import com.wakaztahir.kate.model.model.*

object IntImplementation {

    val propertyMap by lazy { hashMapOf<String, KATEValue>().apply { putObjectFunctions() } }

    private fun HashMap<String, KATEValue>.putObjectFunctions() {
        with(KATEValueImplementation){ putObjectFunctions() }
        put("toDouble", object : KATEFunction() {
            override fun invoke(
                model: KATEObject,
                path: List<ModelReference>,
                pathIndex: Int,
                invokedOn: KATEValue,
                parameters: List<ReferencedValue>
            ): KATEValue {
                val intVal = invokedOn.let { it as? IntValue }?.value
                require(intVal != null) { "int value is null" }
                return DoubleValue(intVal.toDouble())
            }

            override fun toString(): String = "toDouble() : double"
        })
        put("toLong", object : KATEFunction() {
            override fun invoke(
                model: KATEObject,
                path: List<ModelReference>,
                pathIndex: Int,
                invokedOn: KATEValue,
                parameters: List<ReferencedValue>
            ): KATEValue {
                val intVal = invokedOn.let { it as? IntValue }?.value
                require(intVal != null) { "int value is null" }
                return LongValue(intVal.toLong())
            }

            override fun toString(): String = "toLong() : long"
        })
    }

}