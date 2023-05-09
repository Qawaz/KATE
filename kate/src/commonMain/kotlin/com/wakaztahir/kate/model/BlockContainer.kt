package com.wakaztahir.kate.model

import com.wakaztahir.kate.model.model.KATEObject

interface BlockContainer : AtDirective {

    fun getBlockValue(model : KATEObject) : LazyBlock? = null

}