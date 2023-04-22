package com.wakaztahir.kate.runtime

import com.wakaztahir.kate.model.*
import com.wakaztahir.kate.model.model.KATEFunction
import com.wakaztahir.kate.model.model.KATEObject
import com.wakaztahir.kate.model.model.KATEValue
import com.wakaztahir.kate.model.model.ReferencedOrDirectValue

object LongImplementation {

    val propertyMap by lazy { hashMapOf<String, KATEValue>().apply { putObjectFunctions() } }

    private fun HashMap<String, KATEValue>.putObjectFunctions() {
        with(KATEValueImplementation){ putObjectFunctions() }
        put("toInt", object : KATEFunction(KATEType.Int) {
            override fun invoke(
                model: KATEObject,
                path: List<ModelReference>,
                pathIndex: Int,
                parent: ReferencedOrDirectValue?,
                invokedOn: ReferencedOrDirectValue,
                parameters: List<KATEValue>
            ): KATEValue {
                val intVal = invokedOn.let { it as? LongValue }?.value
                require(intVal != null) { "long value is null" }
                return IntValue(intVal.toInt())
            }

            override fun toString(): String = "toInt() : int"
        })
        put("toDouble", object : KATEFunction(KATEType.Double) {
            override fun invoke(
                model: KATEObject,
                path: List<ModelReference>,
                pathIndex: Int,
                parent: ReferencedOrDirectValue?,
                invokedOn: ReferencedOrDirectValue,
                parameters: List<KATEValue>
            ): KATEValue {
                val intVal = invokedOn.let { it as? LongValue }?.value
                require(intVal != null) { "long value is null" }
                return DoubleValue(intVal.toDouble())
            }

            override fun toString(): String = "toDouble() : double"
        })
    }

}