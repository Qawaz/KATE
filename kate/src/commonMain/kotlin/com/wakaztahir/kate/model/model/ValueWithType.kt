package com.wakaztahir.kate.model.model

import com.wakaztahir.kate.model.KATEType

class ValueWithType(val ref : KATEValue,val type : KATEType) : ReferencedOrDirectValue by ref {
    override fun getKATEValueAndType(model: KATEObject): Pair<KATEValue, KATEType?> {
        return Pair(ref.getKATEValue(model),type)
    }
}