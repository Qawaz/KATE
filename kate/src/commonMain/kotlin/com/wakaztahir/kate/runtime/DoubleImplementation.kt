package com.wakaztahir.kate.runtime

import com.wakaztahir.kate.model.*
import com.wakaztahir.kate.model.model.*

object DoubleImplementation {

    val propertyMap by lazy { hashMapOf<String, KATEValue>().apply { putObjectFunctions() } }

    private fun HashMap<String, KATEValue>.putObjectFunctions() {
        with(KATEValueImplementation){ putObjectFunctions() }
        put("toInt", object : KATEFunction() {
            override fun invoke(
                model: KATEObject,
                path: List<ModelReference>,
                pathIndex: Int,
                invokedOn: KATEValue,
                parameters: List<ReferencedValue>
            ): KATEValue {
                val intVal = invokedOn.let { it as? DoubleValue }?.value
                require(intVal != null) { "double value is null" }
                return IntValue(intVal.toInt())
            }

            override fun toString(): String = "toInt() : int"
        })
        put("toLong", object : KATEFunction() {
            override fun invoke(
                model: KATEObject,
                path: List<ModelReference>,
                pathIndex: Int,
                invokedOn: KATEValue,
                parameters: List<ReferencedValue>
            ): KATEValue {
                val intVal = invokedOn.let { it as? DoubleValue }?.value
                require(intVal != null) { "double value is null" }
                return LongValue(intVal.toLong())
            }

            override fun toString(): String = "toLong() : long"
        })
    }

}