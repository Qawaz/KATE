package com.wakaztahir.kate.runtime

import com.wakaztahir.kate.model.*
import com.wakaztahir.kate.model.model.*

object IntImplementation {

    val propertyMap by lazy { hashMapOf<String, KATEValue>().apply { putObjectFunctions() } }

    private fun HashMap<String, KATEValue>.putObjectFunctions() {
        with(KATEValueImplementation){ putObjectFunctions() }
        put("toDouble", object : KATEFunction(KATEType.Double,KATEType.Int) {
            override fun invoke(
                model: KATEObject,
                invokedOn: KATEValue,
                explicitType : KATEType?,
                parameters: List<ReferencedOrDirectValue>
            ): KATEValue {
                val intVal = invokedOn.let { it as? IntValue }?.value
                require(intVal != null) { "int value is null" }
                return DoubleValue(intVal.toDouble())
            }

            override fun toString(): String = "toDouble() : double"
        })
        put("toLong", object : KATEFunction(KATEType.Long,KATEType.Int) {
            override fun invoke(
                model: KATEObject,
                invokedOn: KATEValue,
                explicitType : KATEType?,
                parameters: List<ReferencedOrDirectValue>
            ): KATEValue {
                val intVal = invokedOn.let { it as? IntValue }?.value
                require(intVal != null) { "int value is null" }
                return LongValue(intVal.toLong())
            }

            override fun toString(): String = "toLong() : long"
        })
    }

}