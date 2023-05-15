package com.wakaztahir.kate.model

import com.wakaztahir.kate.model.model.MutableKATEObject

interface ModelProvider {

    val model: MutableKATEObject

    class Single(override val model: MutableKATEObject) : ModelProvider

    class Lazy(provider: () -> MutableKATEObject) : ModelProvider {
        override val model: MutableKATEObject by lazy(provider)
    }

    class Changeable(override var model: MutableKATEObject) : ModelProvider

    class LateInit : ModelProvider {
        override lateinit var model: MutableKATEObject
    }

}