package com.wakaztahir.kte.model.model

import com.wakaztahir.kte.dsl.ModelValue
import com.wakaztahir.kte.model.KTEValue
import com.wakaztahir.kte.model.PrimitiveValue

class ModelListImpl<T : KTEValue>(val collection: List<T>) : List<T> by collection, ModelList<T> {

    private val props: MutableMap<String, (List<Any>?) -> ModelValue> by lazy {
        hashMapOf<String, (List<Any>?) -> ModelValue>().apply {
            put("size") { ModelValue(collection.size) }
            put("contains") {
                if (it == null || it.size > 1) {
                    throw IllegalStateException("contains expect's a single parameter")
                }
                @Suppress("UNCHECKED_CAST")
                (ModelValue(collection.contains(it[0] as T)))
            }
        }
    }

    override fun getValue(key: String): PrimitiveValue<*>? {
        return props[key]?.let { return it(null).value }
    }

    override fun getIterable(key: String): ModelList<KTEValue>? {
        return null
    }

    override fun getFunction(key: String): ((List<Any>) -> ModelValue)? {
        return props[key]
    }

    override fun getObject(key: String): TemplateModel? {
        return null
    }

    override fun toString(): String {
        return stringValue(0)
    }

    override fun stringValue(indentationLevel: Int): String {
        return "${indentation(indentationLevel)}[" + collection.joinToString("\n") { it.stringValue(indentationLevel + 1) } + "]"
    }

}