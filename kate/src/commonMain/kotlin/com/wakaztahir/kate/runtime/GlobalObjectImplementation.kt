package com.wakaztahir.kate.runtime

import com.wakaztahir.kate.model.KATEType
import com.wakaztahir.kate.model.model.*

object GlobalObjectImplementation {

    private val consoleObject by lazy {
        MutableKATEObject {
            insertValue("log", object : KATEFunction(KATEType.Unit, KATEType.String) {
                override fun invoke(
                    model: KATEObject,
                    invokedOn: KATEValue,
                    explicitType: KATEType?,
                    parameters: List<ReferencedOrDirectValue>
                ): ReferencedOrDirectValue {
                    for (param in parameters) {
                        (param.asNullablePrimitive(model)?.value as? String)?.let { println(it) }
                    }
                    return KATEUnit
                }

                override fun toString(): String = "log(vararg params : string)"
            })
        }
    }

    private val throwMethod by lazy {
        object : KATEFunction(KATEType.Unit, KATEType.String) {
            override fun invoke(
                model: KATEObject,
                invokedOn: KATEValue,
                explicitType: KATEType?,
                parameters: List<ReferencedOrDirectValue>
            ): ReferencedOrDirectValue {
                val first = parameters.firstOrNull()?.asNullablePrimitive(model)?.let { it.value as? String }
                require(parameters.size == 1 && first != null) {
                    "${toString()} expects a single parameter of type string"
                }
                throw RuntimeException(first)
            }

            override fun toString(): String = "throw(message : string)"
        }
    }

    fun putIntoObject(obj: MutableKATEObject) {
        obj.insertValue("console", consoleObject)
        obj.insertValue("throw", throwMethod)
    }

}