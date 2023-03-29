package com.wakaztahir.kte.model.model

import com.wakaztahir.kte.dsl.UnresolvedValueException
import com.wakaztahir.kte.model.*
import com.wakaztahir.kte.parser.stream.DestinationStream

interface KTEObject : ReferencedValue {

    val objectName: String
    val contained: Map<String, KTEValue>

    fun contains(key: String): Boolean

    private fun List<ModelReference>.pathUntil(prop: ModelReference): String {
        return joinToString(
            separator = ".",
            limit = indexOf(prop) + 1
        )
    }

    fun getModelReferenceValue(model: KTEObject, path: List<ModelReference>, callFunctions: Boolean): KTEValue {
        var currentVal: KTEValue = this
        for (prop in path) {
            when (prop) {
                is ModelReference.FunctionCall -> {
                    (currentVal.getModelReference(prop) as? KTEFunction)?.let { func ->
                        currentVal = if (callFunctions) {
                            if (prop.invokeOnly) {
                                func.invoke(model, currentVal, prop.parametersList)
                                KTEUnit
                            } else {
                                func.invoke(model, currentVal, prop.parametersList)
                            }
                        } else {
                            func
                        }
                    } ?: run {
                        throw UnresolvedValueException("function ${path.pathUntil(prop)} does not exist")
                    }
                }

                is ModelReference.Property -> {
                    currentVal = currentVal.getModelReference(prop) ?: run {
                        if (path.size == 1 && prop.name == "this") {
                            currentVal
                        } else {
                            throw UnresolvedValueException("property ${path.pathUntil(prop)} does not exist on value : $currentVal")
                        }
                    }
                }
            }
        }
        return currentVal
    }

    fun traverse(block: (KTEValue) -> Unit) {
        block(this)
        for (each in contained) {
            when (each.value) {
                is KTEList<*> -> {
                    for (item in (each.value as KTEList<*>).collection) block(item)
                }

                is KTEObject -> {
                    (each.value as KTEObject).traverse(block)
                }

                else -> {
                    block(each.value)
                }
            }
        }
    }

}