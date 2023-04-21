package com.wakaztahir.kate.runtime

import com.wakaztahir.kate.model.*
import com.wakaztahir.kate.model.model.KATEFunction
import com.wakaztahir.kate.model.model.KATEObject
import com.wakaztahir.kate.model.model.KATEValue
import com.wakaztahir.kate.model.model.ReferencedValue

object LongImplementation {

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
                val intVal = invokedOn.let { it as? LongValue }?.value
                require(intVal != null) { "long value is null" }
                return IntValue(intVal.toInt())
            }

            override fun toString(): String = "toInt() : int"
        })
        put("toDouble", object : KATEFunction() {
            override fun invoke(
                model: KATEObject,
                path: List<ModelReference>,
                pathIndex: Int,
                invokedOn: KATEValue,
                parameters: List<ReferencedValue>
            ): KATEValue {
                val intVal = invokedOn.let { it as? LongValue }?.value
                require(intVal != null) { "long value is null" }
                return DoubleValue(intVal.toDouble())
            }

            override fun toString(): String = "toDouble() : double"
        })
    }

}