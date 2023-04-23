package com.wakaztahir.kate.model.model

import com.wakaztahir.kate.dsl.UnresolvedValueException
import com.wakaztahir.kate.model.*

interface KATEObject : KATEValue {

    val objectName: String

    val parent: KATEObject?

    val itemType: KATEType

    val contained: Map<String, KATEValue>

    override fun getKnownKATEType(): KATEType

    fun get(key: String): KATEValue?

    fun findContainingObjectUpwards(reference: ModelReference): KATEObject?

    fun getModelReferenceInTreeUpwards(reference: ModelReference): KATEValue?

    fun contains(key: String): Boolean

    fun containsInAncestors(key: String): Boolean

    private fun List<ModelReference>.pathUntil(index: Int): String = joinToString(separator = ".", limit = index)

    private fun List<ModelReference>.pathUntil(prop: ModelReference): String = pathUntil(indexOf(prop) + 1)

    private fun throwUnresolved(path: List<ModelReference>, index: Int, current: ReferencedOrDirectValue): Nothing {
        if (path[index] is ModelReference.FunctionCall) {
            throw UnresolvedValueException("function ${path.pathUntil(index)} does not exist on value : $current")
        } else {
            throw UnresolvedValueException("property ${path.pathUntil(index)} does not exist on value : $current")
        }
    }

    private fun findResolvedModelReference(container : KATEValue?, current: KATEValue, path: List<ModelReference>, index: Int): KATEValue {
        val value = container?.getModelReference(path[index])
        return when (val prop = path[index]) {
            is ModelReference.FunctionCall -> {
                (value as? KATEFunction)?.invoke(
                    model = this,
                    path = path,
                    pathIndex = index,
                    parent = container,
                    invokedOn = current,
                    parameters = prop.parametersList.map { it.getKATEValue(this) }
                ) ?: throwUnresolved(path, index, current)
            }

            is ModelReference.Property -> {
                value ?: ((when (prop.name) {
                    "this" -> current
                    "parent" -> (current as? KATEObject)?.parent
                    else -> null
                }) ?: throwUnresolved(path, index, current))
            }
        }
    }

    fun getModelReferenceValue(path: List<ModelReference>): ReferencedOrDirectValue {
        var current: KATEValue = this
        var i = 0
        while (i < path.size) {
            current = findResolvedModelReference(
                container = if(i == 0) ((current as KATEObject).findContainingObjectUpwards(path[0])) else current,
                current = current,
                path = path,
                index = i
            )
            i++
        }
        return current
    }

    fun traverse(block: (ReferencedOrDirectValue) -> Unit) {
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