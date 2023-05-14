package com.wakaztahir.kate.model

import com.wakaztahir.kate.model.model.KATEValue
import com.wakaztahir.kate.model.model.ReferencedOrDirectValue

class LazyReferencedValue(private val creator: () -> KATEValue) : ReferencedOrDirectValue {

    private var _value: KATEValue? = null

    private val value: KATEValue
        get() {
            if (_value == null) _value = creator()
            return _value!!
        }

    override fun getKATEValue(): KATEValue = value.getKATEValue()

    override fun getKATEValueAndType(): Pair<KATEValue, KATEType?> = value.getKATEValueAndType()

}