package com.wakaztahir.kate.model.model

import com.wakaztahir.kate.GlobalModelObjectName
import com.wakaztahir.kate.dsl.ModelObjectImpl
import com.wakaztahir.kate.model.*

interface MutableKATEObject : KATEObject {

    fun insertValue(key : String,value : KATEValue): Boolean

    fun setExplicitType(key : String, type : KATEType)

    fun setValueInTreeUpwardsTypeSafely(key: String, value: KATEValue): Boolean

    // Extensions

    fun insertValue(key: String, value: String) = insertValue(key, StringValue(value))

    fun insertValue(key: String, value: Int) = insertValue(key, IntValue(value))

    fun insertValue(key: String, value: Float) = insertValue(key, DoubleValue(value.toDouble()))

    fun insertValue(key: String, value: Double) = insertValue(key, DoubleValue(value))

    fun insertValue(key: String, value: Boolean) = insertValue(key, BooleanValue(value))

    fun insertValue(key: String, value: List<Int>) = insertValue(key, KATEListImpl(value.map { IntValue(it) }, itemType = KATEType.Int))

    interface PutObjectsScope {
        fun putObject(block: MutableKATEObject.() -> Unit)
    }

    fun putObjects(key: String, block: PutObjectsScope.() -> Unit) {
        val objects = mutableListOf<KATEObject>()
        block(object : PutObjectsScope {
            override fun putObject(block: MutableKATEObject.() -> Unit) {
                objects.add(ModelObjectImpl("$key${objects.size}").apply(block))
            }
        })
        insertValue(key, KATEMutableListImpl(objects, itemType = KATEType.Object(itemsType = KATEType.Any)))
    }

    fun putObject(key: String, block: MutableKATEObject.() -> Unit) {
        insertValue(key, ModelObjectImpl(key).apply(block))
    }

    fun changeName(name: String)

    fun rename(key: String, other: String)

    fun removeKey(key: String) : KATEValue?

    fun removeAll()


}

fun MutableKATEObject(name: String = GlobalModelObjectName, block: MutableKATEObject.() -> Unit): MutableKATEObject {
    val modelObj = ModelObjectImpl(objectName = name)
    block(modelObj)
    return modelObj
}