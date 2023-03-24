package com.wakaztahir.kte.parser.stream.languages

import com.wakaztahir.kte.model.*
import com.wakaztahir.kte.model.model.KTEList
import com.wakaztahir.kte.model.model.KTEObject
import com.wakaztahir.kte.parser.stream.DestinationStream
import com.wakaztahir.kte.parser.stream.WritableStream

class KotlinLanguageDestination(override val stream: WritableStream) : DestinationStream {

    override fun write(value: IntValue) {
        stream.write(value.value.toString())
    }

    override fun write(value: DoubleValue) {
        stream.write(value.value.toString() + 'f')
    }

    override fun write(value: BooleanValue) {
        stream.write(if (value.value) "true" else "false")
    }

    override fun write(value: StringValue) {
        stream.write(value.value)
    }

    override fun write(model: KTEObject, value: KTEList<KTEValue>) {
        stream.write("arrayOf(")
        var isFirst = true
        for (single in value) {
            if (!isFirst) stream.write(", ")
            single.writeTo(model, this)
            isFirst = false
        }
        stream.write(')')
    }

    private fun KTEValue.getType(){
        when(this){
            is IntValue -> "Int"
            is DoubleValue -> "Double"
            is BooleanValue -> "Boolean"
            is StringValue -> "String"
            is KTEFunction -> ""
            else -> "Any"
        }
    }

    override fun write(value: KTEObject) {
        stream.write("""data class ${value.objectName}(
            |
            |)""".trimMargin())
    }

}