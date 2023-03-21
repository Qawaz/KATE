package com.wakaztahir.kte.model.model

import com.wakaztahir.kte.dsl.ModelObjectImpl
import com.wakaztahir.kte.dsl.ModelValue
import com.wakaztahir.kte.model.*

interface MutableTemplateModel : TemplateModel {

    // Put Functions

    fun putValue(key: String, value: PrimitiveValue<*>)

    fun putObject(key: String, obj: TemplateModel)

    fun putFunction(key: String, block: (parameters: List<Any>) -> ModelValue)

    fun putIterable(key: String, value: ModelList<KTEValue>)

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
        putIterable(key, ModelListImpl(objects))
    }

    fun putObject(key: String, block: MutableTemplateModel.() -> Unit) {
        putObject(key, ModelObjectImpl().apply(block))
    }

    fun putValue(key: String, value: ReferencedValue) {
        putValue(key, value.getValue(this))
    }

    fun removeKey(key: String)

}