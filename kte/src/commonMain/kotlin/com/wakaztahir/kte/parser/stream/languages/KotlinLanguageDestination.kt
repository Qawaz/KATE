package com.wakaztahir.kte.parser.stream.languages

import com.wakaztahir.kte.model.*
import com.wakaztahir.kte.model.model.KTEList
import com.wakaztahir.kte.model.model.KTEObject
import com.wakaztahir.kte.parser.stream.DestinationStream
import com.wakaztahir.kte.parser.stream.WritableStream

class KotlinLanguageDestination(override val stream: WritableStream) : DestinationStream {

    private var quotesOnString = false
    private var objectCallOnly = false

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
        if (quotesOnString) {
            stream.write('"' + value.value + '"')
        } else {
            stream.write(value.value)
        }
    }

    override fun write(model: KTEObject, value: KTEList<KTEValue>) {
        stream.write("listOf(")
        var isFirst = true
        for (single in value) {
            if (!isFirst) stream.write(", ")
            single.writeTo(model, this)
            isFirst = false
        }
        stream.write(')')
    }

    private fun KTEValue.getType(): String {
        return when (this) {
            is IntValue -> "Int"
            is DoubleValue -> "Double"
            is BooleanValue -> "Boolean"
            is StringValue -> "String"
            is KTEList<*> -> "List<" + (this.firstOrNull()?.getType() ?: "Any") + ">"
            is KTEObject -> "Any"
            is KTEFunction -> "Any"
            else -> "Any"
        }
    }

    private fun writeObjectAsDataClass(value: KTEObject) {
        stream.write("data class ${value.objectName.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }}(\n")
        var first = true
        for (each in value.contained) {
            if (!first) stream.write(",\n")
            stream.write("\t${each.key} : ${each.value.getType()} = ")
            quotesOnString = true
            each.value.writeTo(value, this)
            quotesOnString = false
            first = false
        }
        stream.write("\n)")
    }

    private fun writeObjectCallOnly(value: KTEObject) {
        stream.write("${value.objectName.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }}()")
    }

    override fun write(value: KTEObject) {
        if (objectCallOnly) {
            writeObjectCallOnly(value)
        } else {
            objectCallOnly = true
            value.traverse {
                if (it is KTEObject) {
                    writeObjectAsDataClass(it)
                    if(it.contained.isNotEmpty()) stream.write("\n\n")
                }
            }
            objectCallOnly = false
        }
    }

}