package com.wakaztahir.kate.model.model

import com.wakaztahir.kate.GlobalModelObjectName
import com.wakaztahir.kate.dsl.ModelObjectImpl
import com.wakaztahir.kate.model.*

interface MutableKATEObject : KATEObject {

    fun setValue(key: String, value: KATEValue): Boolean

    fun setValueInTreeUpwardsTypeSafely(key: String, value: KATEValue): Boolean

    // Put Functions

    @Deprecated("use insertValue", replaceWith = ReplaceWith("setValue(key,value)"))
    fun putValue(key: String, value: KATEValue) {
        setValue(key, value)
    }

    fun setVariableType(key: String, type: KATEType)

    // Extensions

    @Deprecated("use setValue", replaceWith = ReplaceWith("setValue(key,value)"))
    fun putValue(key: String, value: String) {
        setValue(key, StringValue(value))
    }

    @Deprecated("use setValue", replaceWith = ReplaceWith("setValue(key,value)"))
    fun putValue(key: String, value: Int) {
        setValue(key, IntValue(value))
    }

    @Deprecated("use setValue", replaceWith = ReplaceWith("setValue(key,value)"))
    fun putValue(key: String, value: Float) {
        setValue(key, DoubleValue(value.toDouble()))
    }

    @Deprecated("use setValue", replaceWith = ReplaceWith("setValue(key,value)"))
    fun putValue(key: String, value: Double) {
        setValue(key, DoubleValue(value))
    }

    @Deprecated("use setValue", replaceWith = ReplaceWith("setValue(key,value)"))
    fun putValue(key: String, value: Boolean) {
        setValue(key, BooleanValue(value))
    }

    fun setValue(key: String, value: String) {
        setValue(key, StringValue(value))
    }

    fun setValue(key: String, value: Int) {
        setValue(key, IntValue(value))
    }

    fun setValue(key: String, value: Float) {
        setValue(key, DoubleValue(value.toDouble()))
    }

    fun setValue(key: String, value: Double) {
        setValue(key, DoubleValue(value))
    }

    fun setValue(key: String, value: Boolean) {
        setValue(key, BooleanValue(value))
    }

    fun setValue(key: String, value: List<Int>) {
        setValue(key, KATEListImpl(value.map { IntValue(it) }, itemType = KATEType.Int))
    }

    interface PutObjectsScope {
        fun putObject(block: MutableKATEObject.() -> Unit)
    }

    fun putObjects(key: String, block: PutObjectsScope.() -> Unit) {
        val objects = mutableListOf<KATEObject>()
        block(object : PutObjectsScope {
            override fun putObject(block: MutableKATEObject.() -> Unit) {
                objects.add(ModelObjectImpl("$key${objects.size}", itemType = KATEType.Any).apply(block))
            }
        })
        setValue(key, KATEMutableListImpl(objects, itemType = KATEType.Object(itemType = KATEType.Any)))
    }

    fun putObject(key: String, block: MutableKATEObject.() -> Unit) {
        setValue(key, ModelObjectImpl(key, itemType = KATEType.Any).apply(block))
    }

    fun changeName(name: String)

    fun rename(key: String, other: String)

    fun removeKey(key: String)

    fun removeAll()


}

fun MutableKATEObject(name: String = GlobalModelObjectName, block: MutableKATEObject.() -> Unit): MutableKATEObject {
    val modelObj = ModelObjectImpl(objectName = name, itemType = KATEType.Any)
    block(modelObj)
    return modelObj
}