package com.wakaztahir.kate.model

import com.wakaztahir.kate.model.model.KTEObject

interface BlockContainer : AtDirective {

    fun getBlockValue(model : KTEObject) : LazyBlock?

}