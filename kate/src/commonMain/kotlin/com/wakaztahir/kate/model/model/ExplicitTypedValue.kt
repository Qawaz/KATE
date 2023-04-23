package com.wakaztahir.kate.model.model

import com.wakaztahir.kate.model.KATEType

class ExplicitTypedValue(val value: KATEValue, val type: KATEType) : KATEValue by value {
    override fun getKnownKATEType(): KATEType = type
}