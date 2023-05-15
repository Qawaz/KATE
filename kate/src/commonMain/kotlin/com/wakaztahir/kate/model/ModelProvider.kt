package com.wakaztahir.kate.model

import com.wakaztahir.kate.model.model.MutableKATEObject

interface ModelProvider {

    val model: MutableKATEObject

    class Single(override val model: MutableKATEObject) : ModelProvider

    class Changeable(override var model : MutableKATEObject) : ModelProvider

}