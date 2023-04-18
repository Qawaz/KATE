package com.wakaztahir.kate.runtime

import com.wakaztahir.kate.model.StringValue
import com.wakaztahir.kate.model.model.*

object GlobalObjectImplementation {

    private val consoleObject by lazy {
        MutableKATEObject {
            putValue("log", object : KATEFunction() {
                override fun invoke(
                    model: KATEObject,
                    invokedOn: KATEValue,
                    parameters: List<ReferencedValue>
                ): KATEValue {
                    for (param in parameters) {
                        (param.asNullablePrimitive(model) as? StringValue)?.let { println(it) }
                    }
                    return KATEUnit
                }

                override fun toString(): String = "log(vararg params : string)"
            })
        }
    }

    private val throwMethod by lazy {
        object : KATEFunction() {
            override fun invoke(model: KATEObject, invokedOn: KATEValue, parameters: List<ReferencedValue>): KATEValue {
                val first = parameters.firstOrNull()?.asNullablePrimitive(model)?.let { it as? StringValue }
                require(parameters.size == 1 && first != null) {
                    "${toString()} expects a single parameter of type string"
                }
                throw RuntimeException(first.value)
            }

            override fun toString(): String = "throw(message : string)"
        }
    }

    fun putIntoObject(obj: MutableKATEObject) {
        obj.putValue("console", consoleObject)
        obj.putValue("throw", throwMethod)
    }

}