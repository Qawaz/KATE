package com.wakaztahir.kte.model.runtime

import com.wakaztahir.kte.model.model.KTEValue

object KTEObjectImplementation {
    fun eq(x : KTEValue,y : KTEValue): Boolean {
        return x == y
    }
}