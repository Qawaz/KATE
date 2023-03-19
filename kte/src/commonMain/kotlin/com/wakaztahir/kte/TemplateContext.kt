package com.wakaztahir.kte

import com.wakaztahir.kte.model.ConstantReference
import com.wakaztahir.kte.model.DynamicValue
import com.wakaztahir.kte.model.ModelDirective
import com.wakaztahir.kte.model.StringValue
import com.wakaztahir.kte.parser.parseDynamicValue
import com.wakaztahir.kte.parser.stream.SourceStream
import com.wakaztahir.kte.parser.stream.TextSourceStream

class TemplateContext(stream: SourceStream) {

    constructor(text: String) : this(TextSourceStream(text))

    var stream: SourceStream = stream
        private set

    fun updateStream(stream: SourceStream) {
        this.stream = stream
    }

    private val propertyMap = hashMapOf<String, String>()
    private val embedMap = hashMapOf<String, SourceStream>()

    fun embedStream(path: String, stream: SourceStream) {
        embedMap[path] = stream
    }

    fun getEmbeddedStream(path: String): SourceStream? {
        return embedMap[path]
    }

    internal fun getConstantReference(reference: ConstantReference): DynamicValue<*>? {
        return propertyMap[reference.name]?.let { TextSourceStream(it).parseDynamicValue() }
    }

    internal fun getModelDirectiveValue(directive: ModelDirective): DynamicValue<*>? {
        return StringValue("todomodel." + directive.propertyPath.joinToString("."))
    }

    internal fun storeValue(name: String, property: DynamicValue<*>) {
        propertyMap[name] = property.getValueAsString()
    }

    fun storeValue(name: String, value: String) {
        propertyMap[name] = value
    }


}