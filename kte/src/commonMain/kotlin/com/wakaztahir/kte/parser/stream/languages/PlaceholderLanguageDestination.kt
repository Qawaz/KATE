package com.wakaztahir.kte.parser.stream.languages

import com.wakaztahir.kte.model.*
import com.wakaztahir.kte.model.model.*
import com.wakaztahir.kte.parser.stream.DestinationStream
import com.wakaztahir.kte.parser.stream.PlaceholderManager
import com.wakaztahir.kte.parser.stream.WritableStream

class PlaceholderLanguageDestination(
    private val block: LazyBlock,
    override val stream: WritableStream
) : DestinationStream {

    private var doublePlaceholder: PlaceholderBlock = TextPlaceholderBlock(
        text = "@runtime.print_string(@var(this.toString()))",
        parent = block,
        placeholderName = "Double",
        definitionName = "Double"
    )
    private var intPlaceholder: PlaceholderBlock = TextPlaceholderBlock(
        text = "@runtime.print_string(@var(this.toString()))",
        parent = block,
        placeholderName = "Int",
        definitionName = "Int"
    )
    private var stringPlaceholder: PlaceholderBlock = TextPlaceholderBlock(
        text = "@runtime.print_string(@var(this))",
        parent = block,
        placeholderName = "String",
        definitionName = "String"
    )
    private var charPlaceholder: PlaceholderBlock = TextPlaceholderBlock(
        text = "@runtime.print_char(@var(this))",
        parent = block,
        placeholderName = "Char",
        definitionName = "Char"
    )
    private var booleanPlaceholder: PlaceholderBlock = TextPlaceholderBlock(
        text = "@if(@var(this)) @runtime.print_string(\"true\") @else @runtime.print_string(\"false\") @endif",
        parent = block,
        placeholderName = "Boolean",
        definitionName = "Boolean"
    )
    private var listPlaceholder: PlaceholderBlock = TextPlaceholderBlock(
        text = "@if(@var(this)) @runtime.print_string(\"true\") @else @runtime.print_string(\"false\") @endif",
        parent = block,
        placeholderName = "Boolean",
        definitionName = "Boolean"
    )
    private var mutableListPlaceholder: PlaceholderBlock? = listPlaceholder

    private inline fun getAndOnChange(placeholderName: String, crossinline run: (PlaceholderBlock) -> Unit) {
        block.source.placeholderManager.getPlaceholder(placeholderName)?.also(run)
        block.source.placeholderManager.setPlaceholderEventListener(placeholderName,
            object : PlaceholderManager.PlaceholderEventListener {
                override fun onPlaceholderUndefined() {
                    run(
                        TextPlaceholderBlock(
                            text = "",
                            parent = block,
                            placeholderName = placeholderName,
                            definitionName = "Empty"
                        )
                    )
                }

                override fun onPlaceholderDefined(defined: PlaceholderBlock) {
                    run(defined)
                }
            })
    }

    private fun initializePlaceholders() {
        getAndOnChange("DoubleValue") { doublePlaceholder = it }
        getAndOnChange("IntValue") { intPlaceholder = it }
        getAndOnChange("StringValue") { stringPlaceholder = it }
        getAndOnChange("CharValue") { charPlaceholder = it }
        getAndOnChange("BooleanValue") { booleanPlaceholder = it }
        getAndOnChange("ListValue") { listPlaceholder = it }
        getAndOnChange("MutableListValue") {
            mutableListPlaceholder = if (it.definitionName == "Empty") listPlaceholder else it
        }
    }

    init {
        initializePlaceholders()
    }

    private var quotesOnString = false
    private var objectCallOnly = false

    override fun write(block: LazyBlock, value: IntValue) {
        intPlaceholder.generateTo(block.model, value, this)
    }

    override fun write(block: LazyBlock, value: DoubleValue) {
        doublePlaceholder.generateTo(block.model, value, this)
    }

    override fun write(block: LazyBlock, value: BooleanValue) {
        booleanPlaceholder.generateTo(block.model, value, this)
    }

    override fun write(block: LazyBlock, value: CharValue) {
        charPlaceholder.generateTo(block.model, value, this)
    }

    override fun write(block: LazyBlock, value: StringValue) {
        stringPlaceholder.generateTo(block.model, value, this)
    }

    override fun write(block: LazyBlock, value: KTEList<out KTEValue>) {
        listPlaceholder.generateTo(block.model, value, this)
    }

    override fun write(block: LazyBlock, value: KTEMutableList<out KTEValue>) {
        (mutableListPlaceholder ?: listPlaceholder).generateTo(block.model, value, this)
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

    override fun write(block: LazyBlock, value: KTEObject) {
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