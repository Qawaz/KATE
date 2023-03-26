package com.wakaztahir.kte.parser.stream.languages

import com.wakaztahir.kte.model.*
import com.wakaztahir.kte.model.model.KTEFunction
import com.wakaztahir.kte.model.model.KTEList
import com.wakaztahir.kte.model.model.KTEObject
import com.wakaztahir.kte.parser.stream.DestinationStream
import com.wakaztahir.kte.parser.stream.WritableStream

class KotlinLanguageDestination(private val block: LazyBlock, override val stream: WritableStream) : DestinationStream {

    private var quotesOnString = false
    private var objectCallOnly = false

    override fun write(value: IntValue) {
        stream.write(value.value.toString())
    }

    override fun write(value: DoubleValue) {
        stream.write(value.value.toString())
    }

    override fun write(value: BooleanValue) {
        stream.write(if (value.value) "true" else "false")
    }

    override fun write(value: CharValue) {
        stream.write('\'')
        stream.write(value.value)
        stream.write('\'')
    }

    override fun write(value: StringValue) {
        if (quotesOnString) {
            stream.write('"' + value.value + '"')
        } else {
            stream.write(value.value)
        }
    }

    override fun writeList(value: KTEList<out KTEValue>) {
        stream.write("listOf(")
        var isFirst = true
        for (single in value.collection) {
            if (!isFirst) stream.write(", ")
            single.generateTo(block, this)
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
            is KTEList<*> -> "List<" + (this.collection.firstOrNull()?.getType() ?: "Any") + ">"
            is KTEObject -> "Any"
            is KTEFunction -> "Any"
            else -> "Any"
        }
    }

    private fun writeObjectAsDataClass(value: KTEObject) {
        stream.write("data class ${value.objectName}(\n")
        var first = true
        for (each in value.contained) {
            if (!first) stream.write(",\n")
            stream.write("\t${each.key} : ${each.value.getType()} = ")
            quotesOnString = true
            each.value.generateTo(block, this)
            quotesOnString = false
            first = false
        }
        stream.write("\n)")
    }

    private fun writeObjectCallOnly(value: KTEObject) {
        stream.write("${value.objectName}()")
    }

    override fun write(value: KTEObject) {
        if (objectCallOnly) {
            writeObjectCallOnly(value)
        } else {
            objectCallOnly = true
            value.traverse { it ->
                if (it is KTEObject) {
                    writeObjectAsDataClass(it)
                    if (it.contained.any { entry -> entry.value is KTEObject }) stream.write("\n\n")
                }
            }
            objectCallOnly = false
        }
    }

}