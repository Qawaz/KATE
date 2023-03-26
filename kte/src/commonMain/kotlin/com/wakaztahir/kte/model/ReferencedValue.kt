package com.wakaztahir.kte.model

import com.wakaztahir.kte.model.model.KTEObject
import com.wakaztahir.kte.model.model.KTEValue

interface ReferencedValue {
    fun getKTEValue(model: KTEObject): KTEValue
}