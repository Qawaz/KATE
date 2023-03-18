package com.wakaztahir.kte.model

import com.wakaztahir.kte.TemplateContext

enum class IfType(val order: Int) {
    If(0),
    ElseIf(1),
    Else(2)
}

internal class SingleIf(
    val condition: Condition,
    val type: IfType,
    val blockValue: String,
) {
    fun parseBlock(context: TemplateContext): Block {
        TODO("Not yet implemented")
    }
}


internal class IfStatement(private val ifs: MutableList<SingleIf>) {

    val singleIfs: List<SingleIf> get() = ifs

    fun parseBlock(context: TemplateContext): Block {
        TODO("Not yet implemented")
    }

    private fun sortByOrder() {
        ifs.sortBy { it.type.order }
    }

    fun evaluate(context: TemplateContext): SingleIf? {
        sortByOrder()
        for (iffy in ifs) {
            if (iffy.condition.evaluate(context)) {
                return iffy
            }
        }
        return null
    }
}