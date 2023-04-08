package com.wakaztahir.kate.runtime

import com.wakaztahir.kate.model.StringValue
import com.wakaztahir.kate.model.model.KATEFunction
import com.wakaztahir.kate.model.model.KATEObject
import com.wakaztahir.kate.model.model.KATEValue
import com.wakaztahir.kate.model.model.ReferencedValue

object KATEValueImplementation {

    fun HashMap<String, KATEValue>.putObjectFunctions() {
        put("toString", object : KATEFunction() {
            override fun invoke(model: KATEObject, invokedOn: KATEValue, parameters: List<ReferencedValue>): KATEValue {
                return StringValue(invokedOn.toString())
            }

            override fun toString(): String = "toString() : string"
        })
    }

}