package com.wakaztahir.kte.parser.stream.languages

import com.wakaztahir.kte.model.*
import com.wakaztahir.kte.model.model.*
import com.wakaztahir.kte.parser.stream.DestinationStream
import com.wakaztahir.kte.parser.stream.PlaceholderManager
import com.wakaztahir.kte.parser.stream.WritableStream

@Suppress("PrivatePropertyName")
class PlaceholderLanguageDestination(
    private val block: LazyBlock,
    override val stream: WritableStream
) : DestinationStream {

    private val DoublePlaceholderName = "Double"
    private val IntPlaceholderName = "Int"
    private val StringPlaceholderName = "String"
    private val CharPlaceholderName = "Char"
    private val BooleanPlaceholderName = "Boolean"
    private val ListPlaceholderName = "List"
    private val MutableListPlaceholderName = "MutableList"
    private val ObjectPlaceholderName = "Object"

    private var doublePlaceholder: PlaceholderBlock = TextPlaceholderBlock(
        text = "@runtime.print_string(@var(__param__.toString()))",
        parent = block,
        placeholderName = DoublePlaceholderName,
        definitionName = DoublePlaceholderName
    )
    private var intPlaceholder: PlaceholderBlock = TextPlaceholderBlock(
        text = "@runtime.print_string(@var(__param__.toString()))",
        parent = block,
        placeholderName = IntPlaceholderName,
        definitionName = IntPlaceholderName
    )
    private var stringPlaceholder: PlaceholderBlock = TextPlaceholderBlock(
        text = "@runtime.print_string(@var(__param__))",
        parent = block,
        placeholderName = StringPlaceholderName,
        definitionName = StringPlaceholderName
    )
    private var charPlaceholder: PlaceholderBlock = TextPlaceholderBlock(
        text = "@runtime.print_char(@var(__param__))",
        parent = block,
        placeholderName = CharPlaceholderName,
        definitionName = CharPlaceholderName
    )
    private var booleanPlaceholder: PlaceholderBlock = TextPlaceholderBlock(
        text = "@if(@var(__param__)) @runtime.print_string(\"true\") @else @runtime.print_string(\"false\") @endif",
        parent = block,
        placeholderName = BooleanPlaceholderName,
        definitionName = BooleanPlaceholderName
    )
    private var listPlaceholder: PlaceholderBlock = TextPlaceholderBlock(
        text = "@runtime.print_string(@var(__param__.joinToString()))",
        parent = block,
        placeholderName = ListPlaceholderName,
        definitionName = ListPlaceholderName
    )
    private var mutableListPlaceholder: PlaceholderBlock = TextPlaceholderBlock(
        text = "@runtime.print_string(@var(__param__.joinToString()))",
        parent = block,
        placeholderName = MutableListPlaceholderName,
        definitionName = MutableListPlaceholderName
    )
    private var objectPlaceholder: PlaceholderBlock = TextPlaceholderBlock(
        text = "@runtime.print_string(@var(__param__.toString()))",
        parent = block,
        placeholderName = ObjectPlaceholderName,
        definitionName = ObjectPlaceholderName
    )

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
        getAndOnChange(DoublePlaceholderName) { doublePlaceholder = it }
        getAndOnChange(IntPlaceholderName) { intPlaceholder = it }
        getAndOnChange(StringPlaceholderName) { stringPlaceholder = it }
        getAndOnChange(CharPlaceholderName) { charPlaceholder = it }
        getAndOnChange(BooleanPlaceholderName) { booleanPlaceholder = it }
        getAndOnChange(ListPlaceholderName) { listPlaceholder = it }
        getAndOnChange(MutableListPlaceholderName) { mutableListPlaceholder = it }
        getAndOnChange(ObjectPlaceholderName) { objectPlaceholder = it }
    }

    init {
        initializePlaceholders()
    }

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
        mutableListPlaceholder.generateTo(block.model, value, this)
    }

    override fun write(block: LazyBlock, value: KTEObject) {
        objectPlaceholder.generateTo(block.model, value, this)
    }

}