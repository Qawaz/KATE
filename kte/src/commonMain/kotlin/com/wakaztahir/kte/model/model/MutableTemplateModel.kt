package com.wakaztahir.kte.model.model

import com.wakaztahir.kte.dsl.ModelObjectImpl
import com.wakaztahir.kte.model.*

abstract class MutableTemplateModel : TemplateModel {

    // Put Functions

    abstract fun contains(key : String) : Boolean

    abstract fun putValue(key: String, value: KTEValue)

    // Extensions

    fun putValue(key: String, value: String) {
        putValue(key, StringValue(value))
    }

    fun putValue(key: String, value: Int) {
        putValue(key, IntValue(value))
    }

    fun putValue(key: String, value: Float) {
        putValue(key, FloatValue(value))
    }

    fun putValue(key: String, value: Boolean) {
        putValue(key, BooleanValue(value))
    }

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
        fun putObject(block: MutableTemplateModel.() -> Unit)
    }

    fun putObjects(key: String, block: PutObjectsScope.() -> Unit) {
        val objects = mutableListOf<TemplateModel>()
        block(object : PutObjectsScope {
            override fun putObject(block: MutableTemplateModel.() -> Unit) {
                objects.add(ModelObjectImpl().apply(block))
            }
        })
        putValue(key, ModelListImpl(objects))
    }

    fun putObject(key: String, block: MutableTemplateModel.() -> Unit) {
        putValue(key, ModelObjectImpl().apply(block))
    }

    abstract fun removeKey(key: String)

}