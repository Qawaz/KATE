package com.wakaztahir.kate.runtime

import com.wakaztahir.kate.model.StringValue
import com.wakaztahir.kate.model.model.KATEFunction
import com.wakaztahir.kate.model.model.KATEObject
import com.wakaztahir.kate.model.model.KATEValue
import com.wakaztahir.kate.model.model.ReferencedValue

object BooleanImplementation {
    val propertyMap by lazy { hashMapOf<String, KATEValue>().apply { putObjectFunctions() } }
    private fun HashMap<String, KATEValue>.putObjectFunctions() {
        with(KATEValueImplementation){ putObjectFunctions() }
    }
}