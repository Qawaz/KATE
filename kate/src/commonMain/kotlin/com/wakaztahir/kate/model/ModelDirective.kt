package com.wakaztahir.kate.model

import com.wakaztahir.kate.dsl.UnresolvedValueException
import com.wakaztahir.kate.model.model.*

sealed interface ModelReference {

    val name: String

    class Property(override val name: String) : ModelReference {
        override fun toString(): String {
            return name
        }
    }

    class FunctionCall(
        override val name: String,
        val parametersList: List<ReferencedOrDirectValue>
    ) : ModelReference {

        override fun toString(): String {
            return name + '(' + parametersList.joinToString(",") + ')'
        }

//        fun satisfies(type: KATEType): Boolean {
//            val actual = if (type is KATEType.NullableKateType) type.actual else type
//            if (actual is KATEType.Any) return true
//            if (actual is KATEType.Function) {
//                if (actual.parameterTypes == null) return parametersList.isEmpty()
//                if (actual.parameterTypes.size != parametersList.size) return false
//                // not done yet , don't know the type of parameter
//            }
//            return false
//        }

    }

}

class ModelDirective(override val propertyPath: List<ModelReference>, override val provider: ModelProvider) :
    ReferencedValue {

    init {
        require(propertyPath.isNotEmpty()) {
            "model directive with empty path is not allowed"
        }
    }

    private fun KATEObject.root(): KATEObject = if (this.parent == null) this else this.parent!!.root()

    private val KATEObject.container
        get() = this.findContainingObjectUpwards(propertyPath[0])
            ?: this.findInternalReferenceProperty(propertyPath[0]) as? KATEObject
            ?: throw UnresolvedValueException("property ${propertyPath[0]} from path $propertyPath couldn't be found in root tree ${this.root()}")

    private fun getModelReferenceValueAndType(model: KATEObject): Pair<KATEValue, KATEType?> {
        var i = 0
        var current: Pair<KATEValue, KATEType?> = Pair(model.container, null)
        while (i < propertyPath.size) {
            current = model.findResolvedModelReference(
                current = current.first,
                explicitType = current.second,
                path = propertyPath,
                index = i
            )
            i++
        }
        return current
    }

    override fun getKATEValue(): KATEValue {
        return getModelReferenceValueAndType(provider.model).first
    }

    override fun getKATEValueAndType(): Pair<KATEValue, KATEType?> {
        return getModelReferenceValueAndType(provider.model)
    }

    override fun toString(): String = propertyPath.joinToString(".") + '=' + getKATEValue()

}