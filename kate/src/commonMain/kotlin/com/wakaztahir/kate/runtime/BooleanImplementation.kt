package com.wakaztahir.kate.runtime

import com.wakaztahir.kate.model.model.KATEValue
import com.wakaztahir.kate.model.model.ReferencedOrDirectValue

object BooleanImplementation {
    val propertyMap by lazy { hashMapOf<String, KATEValue>().apply { putObjectFunctions() } }
    private fun HashMap<String, KATEValue>.putObjectFunctions() {
        with(KATEValueImplementation){ putObjectFunctions() }
    }
}