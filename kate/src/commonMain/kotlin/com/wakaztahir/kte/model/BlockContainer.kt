package com.wakaztahir.kte.model

import com.wakaztahir.kte.model.model.KTEObject

interface BlockContainer : AtDirective {

    fun getBlockValue(model : KTEObject) : LazyBlock?

}