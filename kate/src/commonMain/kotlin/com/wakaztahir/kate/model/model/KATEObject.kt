package com.wakaztahir.kate.model.model

import com.wakaztahir.kate.dsl.UnresolvedValueException
import com.wakaztahir.kate.model.*
import com.wakaztahir.kate.runtime.KATEObjectImplementation

interface KATEObject : ReferencedValue {

    val objectName: String
    val parent: KATEObject?
    val contained: Map<String, KATEValue>

    fun get(key: String): KATEValue?

    fun getExplicitTypeInTreeUpwards(key: String): KATEType?

    fun getExplicitType(key: String): KATEType?

    fun contains(key: String): Boolean

    fun containsInAncestors(key: String): Boolean

    private fun List<ModelReference>.pathUntil(prop: ModelReference): String {
        return joinToString(
            separator = ".",
            limit = indexOf(prop) + 1
        )
    }

    fun getModelReferenceInTreeUpwards(reference: ModelReference): KATEValue?

    private fun throwUnresolved(path: List<ModelReference>, prop: ModelReference, current: KATEValue): Nothing {
        if (prop is ModelReference.FunctionCall) {
            throw UnresolvedValueException("function ${path.pathUntil(prop)} does not exist on value : $current")
        } else {
            throw UnresolvedValueException("property ${path.pathUntil(prop)} does not exist on value : $current")
        }
    }

    private fun KATEValue.getModelReference(index: Int, ref: ModelReference): KATEValue? {
        return if (index == 0) {
            (this as KATEObject).getModelReferenceInTreeUpwards(ref)
        } else {
            this.getModelReference(ref)
        }
    }

    fun getModelReferenceValue(model: KATEObject, path: List<ModelReference>): KATEValue {
        var currentVal: KATEValue = this
        var i = 0
        while (i < path.size) {
            when (val prop = path[i]) {
                is ModelReference.FunctionCall -> {
                    (currentVal.getModelReference(i, prop) as? KATEFunction)?.let { func ->
                        currentVal = func.invoke(model, path, i, currentVal, prop.parametersList)
                    } ?: throwUnresolved(path, prop, currentVal)
                }

                is ModelReference.Property -> {
                    currentVal = currentVal.getModelReference(i, prop) ?: run {
                        when (prop.name) {
                            "this" -> {
                                currentVal
                            }

                            "parent" -> {
                                (currentVal as? KATEObject)?.parent ?: throwUnresolved(path, prop, currentVal)
                            }

                            else -> throwUnresolved(path, prop, currentVal)
                        }
                    }
                }
            }
            i++
        }
        return currentVal
    }

    fun traverse(block: (KATEValue) -> Unit) {
        block(this)
        for (each in contained) {
            when (each.value) {
                is KATEList<*> -> {
                    for (item in (each.value as KATEList<*>).collection) block(item)
                }

                is KATEObject -> {
                    (each.value as KATEObject).traverse(block)
                }

                else -> {
                    block(each.value)
                }
            }
        }
    }

}