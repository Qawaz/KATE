package com.wakaztahir.kate.model.model

import com.wakaztahir.kate.GlobalModelObjectName
import com.wakaztahir.kate.dsl.ModelObjectImpl
import com.wakaztahir.kate.model.*

interface MutableKATEObject : KATEObject {

    fun insertValue(key : String,value : KATEValue): Boolean

    @Deprecated("use insertValue with type", replaceWith = ReplaceWith(expression = "insertValue(key,value)"))
    fun setValue(key: String, value: KATEValue): Boolean

    fun setValueInTreeUpwardsTypeSafely(key: String, value: KATEValue): Boolean

    // Extensions

    fun insertValue(key: String, value: String) {
        insertValue(key, StringValue(value))
    }

    fun insertValue(key: String, value: Int) {
        insertValue(key, IntValue(value))
    }

    fun insertValue(key: String, value: Float) {
        insertValue(key, DoubleValue(value.toDouble()))
    }

    fun insertValue(key: String, value: Double) {
        insertValue(key, DoubleValue(value))
    }

    fun insertValue(key: String, value: Boolean) {
        insertValue(key, BooleanValue(value))
    }

    fun insertValue(key: String, value: List<Int>) {
        insertValue(key, KATEListImpl(value.map { IntValue(it) }, itemType = KATEType.Int))
    }

    @Deprecated("use insertValue", replaceWith = ReplaceWith("setValue(key,value)"))
    fun putValue(key: String, value: KATEValue) {
        insertValue(key, value)
    }

    @Deprecated("use insertValue", replaceWith = ReplaceWith("insertValue(key,value)"))
    fun putValue(key: String, value: String) {
        insertValue(key, StringValue(value))
    }

    @Deprecated("use insertValue", replaceWith = ReplaceWith("insertValue(key,value)"))
    fun putValue(key: String, value: Int) {
        insertValue(key, IntValue(value))
    }

    @Deprecated("use insertValue", replaceWith = ReplaceWith("insertValue(key,value)"))
    fun putValue(key: String, value: Float) {
        insertValue(key, DoubleValue(value.toDouble()))
    }

    @Deprecated("use insertValue", replaceWith = ReplaceWith("insertValue(key,value)"))
    fun putValue(key: String, value: Double) {
        insertValue(key, DoubleValue(value))
    }

    @Deprecated("use insertValue", replaceWith = ReplaceWith("insertValue(key,value)"))
    fun putValue(key: String, value: Boolean) {
        insertValue(key, BooleanValue(value))
    }

    @Deprecated("use insertValue", replaceWith = ReplaceWith("insertValue(key,value)"))
    fun setValue(key: String, value: String) {
        insertValue(key, StringValue(value))
    }

    @Deprecated("use insertValue", replaceWith = ReplaceWith("insertValue(key,value)"))
    fun setValue(key: String, value: Int) {
        insertValue(key, IntValue(value))
    }

    @Deprecated("use insertValue", replaceWith = ReplaceWith("insertValue(key,value)"))
    fun setValue(key: String, value: Float) {
        insertValue(key, DoubleValue(value.toDouble()))
    }

    @Deprecated("use insertValue", replaceWith = ReplaceWith("insertValue(key,value)"))
    fun setValue(key: String, value: Double) {
        insertValue(key, DoubleValue(value))
    }

    @Deprecated("use insertValue", replaceWith = ReplaceWith("insertValue(key,value)"))
    fun setValue(key: String, value: Boolean) {
        insertValue(key, BooleanValue(value))
    }

    @Deprecated("use insertValue", replaceWith = ReplaceWith("insertValue(key,value)"))
    fun setValue(key: String, value: List<Int>) {
        insertValue(key, KATEListImpl(value.map { IntValue(it) }, itemType = KATEType.Int))
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
        insertValue(key, KATEMutableListImpl(objects, itemType = KATEType.Object(itemType = KATEType.Any)))
    }

    fun putObject(key: String, block: MutableKATEObject.() -> Unit) {
        insertValue(key, ModelObjectImpl(key, itemType = KATEType.Any).apply(block))
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