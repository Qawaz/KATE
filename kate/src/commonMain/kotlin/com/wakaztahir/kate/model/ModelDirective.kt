package com.wakaztahir.kate.model

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

class ModelDirective(override val propertyPath: List<ModelReference>, override val referenceModel: KATEObject) :
    ReferencedValue {

    init {
        require(propertyPath.isNotEmpty()) {
            "model directive with empty path is not allowed"
        }
    }

    override fun getKATEValue(model: KATEObject): KATEValue {
        return model.getModelReferenceValue(path = propertyPath)
    }

    override fun getKATEValueAndType(model: KATEObject): Pair<KATEValue, KATEType?> {
        return model.getModelReferenceValueAndType(path = propertyPath)
    }

    override fun toString(): String = propertyPath.joinToString(".")

}