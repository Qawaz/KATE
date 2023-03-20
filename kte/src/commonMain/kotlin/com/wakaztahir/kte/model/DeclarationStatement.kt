package com.wakaztahir.kte.model

import com.wakaztahir.kte.dsl.ModelDsl

interface DeclarationStatement {
    fun storeValue(model: ModelDsl)
}