package com.wakaztahir.kte.model

import com.wakaztahir.kte.model.model.KTEObject

interface BlockContainer : CodeGen {

    fun getBlockValue(model : KTEObject) : LazyBlock?

}