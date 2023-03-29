package com.wakaztahir.kte.model.runtime

import com.wakaztahir.kte.model.DoubleValue
import com.wakaztahir.kte.model.StringValue
import com.wakaztahir.kte.model.model.KTEFunction
import com.wakaztahir.kte.model.model.KTEObject
import com.wakaztahir.kte.model.model.KTEValue
import com.wakaztahir.kte.model.model.ReferencedValue

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