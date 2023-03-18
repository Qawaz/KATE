package com.wakaztahir.kte

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

    fun storeValue(name: String, value: String) {
        propertyMap[name] = value
    }

}