package com.wakaztahir.kate.runtime

import com.wakaztahir.kate.model.StringValue
import com.wakaztahir.kate.model.model.KTEFunction
import com.wakaztahir.kate.model.model.KTEObject
import com.wakaztahir.kate.model.model.KTEValue
import com.wakaztahir.kate.model.model.ReferencedValue

object CharImplementation {

    val propertyMap by lazy { hashMapOf<String, KTEValue>().apply { putObjectFunctions() } }

    private fun HashMap<String, KTEValue>.putObjectFunctions() {
        put("getType", object : KTEFunction() {
            override fun invoke(model: KTEObject, invokedOn: KTEValue, parameters: List<ReferencedValue>): KTEValue {
                return StringValue("char")
            }
            override fun toString(): String = "getType() : string"
        })
    }
}