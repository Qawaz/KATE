package com.wakaztahir.kate.model.model

import com.wakaztahir.kate.dsl.UnresolvedValueException
import com.wakaztahir.kate.model.*

interface KATEObject : KATEValue {

    val objectName: String

    val parent: KATEObject?

    fun getItemsType(): KATEType

    override fun getKnownKATEType(): KATEType

    fun get(key: String): KATEValue?

    fun getExplicitType(key: String): KATEType?

    fun getKeys(): Collection<String>

    fun getValues(): Collection<KATEValue>

    fun findContainingObjectUpwards(reference: ModelReference): KATEObject?

    fun getModelReferenceInTreeUpwards(reference: ModelReference): KATEValue?

    fun contains(key: String): Boolean

    fun containsInAncestors(key: String): Boolean

    private fun List<ModelReference>.pathUntil(index: Int): String = joinToString(separator = ".", limit = index)

    private fun List<ModelReference>.pathUntil(prop: ModelReference): String = pathUntil(indexOf(prop) + 1)

    private fun throwUnresolved(path: List<ModelReference>, index: Int, current: ReferencedOrDirectValue): Nothing {
        if (path[index] is ModelReference.FunctionCall) {
            throw UnresolvedValueException("function ${path.pathUntil(index + 1)} does not exist on value : $current")
        } else {
            throw UnresolvedValueException("property ${path.pathUntil(index + 1)} does not exist on value : $current")
        }
    }

    fun findInternalReferenceProperty(ref: ModelReference): KATEValue? {
        return when (ref) {
            is ModelReference.FunctionCall -> null
            is ModelReference.Property -> {
                when (ref.name) {
                    "this" -> this
                    "parent" -> (this.asNullableObject(this))?.parent
                    else -> null
                }
            }
        }
    }

    fun findResolvedModelReference(
        current: KATEValue,
        explicitType: KATEType?,
        path: List<ModelReference>,
        index: Int
    ): Pair<KATEValue, KATEType?> {
        val value = current.getModelReference(path[index])
        val valueType = if (current is KATEObject) {
            current.getExplicitType(path[index].name)
        } else null
        return when (val prop = path[index]) {
            is ModelReference.FunctionCall -> {
                (value as? KATEFunction)?.invoke(
                    model = this,
                    invokedOn = current,
                    explicitType = explicitType,
                    parameters = prop.parametersList
                )?.getKATEValueAndType(this) ?: throwUnresolved(path, index, current)
            }

            is ModelReference.Property -> {
                (value?.let { Pair(it, valueType) })
                    ?: (((current as? KATEObject)?.findInternalReferenceProperty(ref = prop)?.let { Pair(it, null) })
                        ?: throwUnresolved(path, index, current))
            }
        }
    }

}