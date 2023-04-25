package com.wakaztahir.kate.runtime

import com.wakaztahir.kate.model.KATEType
import com.wakaztahir.kate.model.StringValue
import com.wakaztahir.kate.model.model.KATEFunction
import com.wakaztahir.kate.model.model.KATEObject
import com.wakaztahir.kate.model.model.KATEValue
import com.wakaztahir.kate.model.model.ReferencedOrDirectValue

object KATEValueImplementation {

    val propertyMap: HashMap<String, KATEValue> = hashMapOf<String, KATEValue>().apply { putObjectFunctions() }

    fun HashMap<String, KATEValue>.putObjectFunctions() {
        put("getType", object : KATEFunction(KATEType.String) {
            override fun invoke(
                model: KATEObject,
                invokedOn: KATEValue,
                explicitType: KATEType?,
                parameters: List<ReferencedOrDirectValue>
            ): ReferencedOrDirectValue {
                return StringValue(
                    value = (explicitType ?: invokedOn.getKnownKATEType()).getKATEType()
                )
            }

            override fun toString(): String = "getType() : string"
        })
        put("toString", object : KATEFunction(KATEType.String) {
            override fun invoke(
                model: KATEObject,
                invokedOn: KATEValue,
                explicitType: KATEType?,
                parameters: List<ReferencedOrDirectValue>
            ): ReferencedOrDirectValue {
                return StringValue(invokedOn.toString())
            }

            override fun toString(): String = "toString() : string"
        })
    }

}