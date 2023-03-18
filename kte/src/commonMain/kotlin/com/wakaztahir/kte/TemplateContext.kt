package com.wakaztahir.kte

import com.wakaztahir.kte.model.DynamicValue
import com.wakaztahir.kte.parser.DynamicProperty
import com.wakaztahir.kte.parser.stream.SourceStream

class TemplateContext(stream: SourceStream) {

    var stream: SourceStream = stream
        private set

    fun updateStream(stream: SourceStream) {
        this.stream = stream
    }

    private val propertyMap = hashMapOf<String, String>()

    fun getPropertyValue(name: String): String? {
        return propertyMap[name]
    }

    internal fun storeValue(name: String, property: DynamicValue<*>) {
        propertyMap[name] = property.getValueAsString()
    }

    fun storeValue(name: String, value: String) {
        propertyMap[name] = value
    }

}