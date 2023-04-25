package com.wakaztahir.kate.model

import com.wakaztahir.kate.model.model.KATEObject
import com.wakaztahir.kate.model.model.KATEValue
import com.wakaztahir.kate.model.model.ReferencedOrDirectValue

class LazyReferencedValue(private val creator: () -> KATEValue) : ReferencedOrDirectValue {

    private var _value: KATEValue? = null

    private val value: KATEValue
        get() {
            if (_value == null) _value = creator()
            return _value!!
        }

    override fun getKATEValue(model: KATEObject): KATEValue = value.getKATEValue(model)

    override fun getKATEValueAndType(model: KATEObject): Pair<KATEValue, KATEType?> = value.getKATEValueAndType(model)

}