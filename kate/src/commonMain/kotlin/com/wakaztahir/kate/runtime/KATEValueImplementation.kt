package com.wakaztahir.kate.runtime

import com.wakaztahir.kate.model.DoubleValue
import com.wakaztahir.kate.model.KATEType
import com.wakaztahir.kate.model.ModelReference
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
                path: List<ModelReference>,
                pathIndex: Int,
                parent: ReferencedOrDirectValue?,
                invokedOn: KATEValue,
                parameters: List<KATEValue>
            ): KATEValue {
                return StringValue(
                    value = invokedOn.getKnownKATEType().getKATEType()
                )
            }

            override fun toString(): String = "getType() : string"
        })
        put("toString", object : KATEFunction(KATEType.String) {
            override fun invoke(
                model: KATEObject,
                path: List<ModelReference>,
                pathIndex: Int,
                parent: ReferencedOrDirectValue?,
                invokedOn: KATEValue,
                parameters: List<KATEValue>
            ): KATEValue {
                return StringValue(invokedOn.toString())
            }

            override fun toString(): String = "toString() : string"
        })
    }

}