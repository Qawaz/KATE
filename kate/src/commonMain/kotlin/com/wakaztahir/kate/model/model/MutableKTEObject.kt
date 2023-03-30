package com.wakaztahir.kate.model.model

import com.wakaztahir.kate.GlobalModelObjectName
import com.wakaztahir.kate.dsl.ModelObjectImpl
import com.wakaztahir.kate.model.*

interface MutableKTEObject : KTEObject {

    // Put Functions

    fun putValue(key: String, value: KTEValue)

    // Extensions

    fun putValue(key: String, value: String) {
        putValue(key, StringValue(value))
    }

    fun putValue(key: String, value: Int) {
        putValue(key, IntValue(value))
    }

    fun putValue(key: String, value: Double) {
        putValue(key, DoubleValue(value))
    }

    fun putValue(key: String, value: Boolean) {
        putValue(key, BooleanValue(value))
    }

// TODO this function doesn't work
//    fun copy(other: KTEObject) {
//        for (each in other.contained) putValue(each.key, each.value)
//    }

//    fun putIterable(key: String, value: List<Int>) {
//        putIterable(key, ModelListImpl(value.map { IntValue(it) }))
//    }
//
//    fun putIterable(key: String, value: List<Float>) {
//        putIterable(key, ModelListImpl(value.map { FloatValue(it) }))
//    }
//
//    fun putIterable(key: String, value: List<Boolean>) {
//        putIterable(key, ModelListImpl(value.map { BooleanValue(it) }))
//    }
//
//    fun putIterable(key: String, value: List<String>) {
//        putIterable(key, ModelListImpl(value.map { StringValue(it) }))
//    }

    interface PutObjectsScope {
        fun putObject(block: MutableKTEObject.() -> Unit)
    }

    fun putObjects(key: String, block: PutObjectsScope.() -> Unit) {
        val objects = mutableListOf<KTEObject>()
        block(object : PutObjectsScope {
            override fun putObject(block: MutableKTEObject.() -> Unit) {
                objects.add(ModelObjectImpl("$key${objects.size}").apply(block))
            }
        })
        putValue(key, KTEMutableListImpl(objects))
    }

    fun putObject(key: String, block: MutableKTEObject.() -> Unit) {
        putValue(key, ModelObjectImpl(key).apply(block))
    }

    fun changeName(name : String)

    fun rename(key : String,other : String)

    fun removeKey(key: String)

    fun removeAll()


}

fun MutableKTEObject(name: String = GlobalModelObjectName, block: MutableKTEObject.() -> Unit): MutableKTEObject {
    val modelObj = ModelObjectImpl(objectName = name)
    block(modelObj)
    return modelObj
}