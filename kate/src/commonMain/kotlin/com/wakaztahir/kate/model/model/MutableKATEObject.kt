package com.wakaztahir.kate.model.model

import com.wakaztahir.kate.GlobalModelObjectName
import com.wakaztahir.kate.dsl.ModelObjectImpl
import com.wakaztahir.kate.model.*

interface MutableKATEObject : KATEObject {

    fun setValue(key: String, value: KATEValue): Boolean

    fun setValueInTreeUpwardsTypeSafely(key: String, value: KATEValue): Boolean

    // Put Functions

    @Deprecated("use insertValue", replaceWith = ReplaceWith("insertValue(key,value)"))
    fun putValue(key: String, value: KATEValue)

    fun setExplicitType(key: String, type: KATEType)

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

//    fun putList(key: String, value: List<Int>) {
//        putValue(key, KATEListImpl(value.map { IntValue(it) }))
//    }
//
//    fun putList(key: String, value: List<Float>) {
//        putValue(key, KATEListImpl(value.map { DoubleValue(it.toDouble()) }))
//    }
//
//    fun putList(key: String, value: List<Double>) {
//        putValue(key, KATEListImpl(value.map { DoubleValue(it) }))
//    }
//
//    fun putList(key: String, value: List<Boolean>) {
//        putValue(key, KATEListImpl(value.map { BooleanValue(it) }))
//    }
//
//    fun putList(key: String, value: List<String>) {
//        putValue(key, KATEListImpl(value.map { StringValue(it) }))
//    }

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
        setValue(key, KATEMutableListImpl(objects))
    }

    fun putObject(key: String, block: MutableKATEObject.() -> Unit) {
        setValue(key, ModelObjectImpl(key).apply(block))
    }

    fun changeName(name: String)

    fun rename(key: String, other: String)

    fun removeKey(key: String)

    fun removeAll()


}

fun MutableKATEObject(name: String = GlobalModelObjectName, block: MutableKATEObject.() -> Unit): MutableKATEObject {
    val modelObj = ModelObjectImpl(objectName = name)
    block(modelObj)
    return modelObj
}