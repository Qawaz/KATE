package com.wakaztahir.kate.runtime

import com.wakaztahir.kate.model.ModelReference
import com.wakaztahir.kate.model.StringValue
import com.wakaztahir.kate.model.model.KATEFunction
import com.wakaztahir.kate.model.model.KATEObject
import com.wakaztahir.kate.model.model.KATEValue
import com.wakaztahir.kate.model.model.ReferencedValue

object KATEValueImplementation {

    fun HashMap<String, KATEValue>.putObjectFunctions() {
        put("getType", object : KATEFunction() {
            override fun invoke(
                model: KATEObject,
                path: List<ModelReference>,
                pathIndex: Int,
                invokedOn: KATEValue,
                parameters: List<ReferencedValue>
            ): KATEValue {
                return StringValue(
                    value = path.getOrNull(pathIndex - 1)?.name?.let { model.getExplicitType(it) }?.getKATEType()
                        ?: invokedOn.getKATEType(model).getKATEType()
                )
            }

            override fun toString(): String = "getType() : string"
        })
        put("toString", object : KATEFunction() {
            override fun invoke(
                model: KATEObject,
                path: List<ModelReference>,
                pathIndex: Int,
                invokedOn: KATEValue,
                parameters: List<ReferencedValue>
            ): KATEValue {
                return StringValue(invokedOn.toString())
            }

            override fun toString(): String = "toString() : string"
        })
    }

}