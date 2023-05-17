package com.wakaztahir.kate.runtime

import com.wakaztahir.kate.model.BooleanValue
import com.wakaztahir.kate.model.KATEEnum
import com.wakaztahir.kate.model.KATEType
import com.wakaztahir.kate.model.StringValue
import com.wakaztahir.kate.model.model.*

object KATEEnumImplementation {

    val propertyMap by lazy { hashMapOf<String, KATEValue>().apply { putEnumFunctions() } }

    private val KATEValue.kateEnum: KATEEnum
        get() {
            return this as? KATEEnum
                ?: throw IllegalStateException("value of type ${getKnownKATEType()} is not an enum")
        }

    private fun HashMap<String, KATEValue>.putEnumFunctions() {
        with(KATEValueImplementation) { putObjectFunctions() }
        put("getKeys", object : KATEFunction(KATEType.List(KATEType.String)) {
            override fun invoke(
                model: KATEObject,
                invokedOn: KATEValue,
                explicitType: KATEType?,
                parameters: List<ReferencedOrDirectValue>
            ): ReferencedOrDirectValue {
                return invokedOn.kateEnum.values
            }

            override fun toString(): String = "getKeys() : List<string>"

        })
    }


}