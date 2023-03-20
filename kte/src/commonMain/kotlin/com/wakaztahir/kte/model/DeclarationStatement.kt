package com.wakaztahir.kte.model

import com.wakaztahir.kte.model.model.MutableTemplateModel

interface DeclarationStatement {
    fun storeValue(model: MutableTemplateModel)
}