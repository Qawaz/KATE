package com.wakaztahir.kte.model

import com.wakaztahir.kte.TemplateContext
import com.wakaztahir.kte.parser.stream.DestinationStream


internal enum class ConditionType {

    Equals {
        override fun verifyCompare(result: Int) = result == 0
    },
    NotEquals {
        override fun verifyCompare(result: Int) = result != 0
    },
    GreaterThan {
        override fun verifyCompare(result: Int) = result == 1
    },
    LessThan {
        override fun verifyCompare(result: Int) = result == -1
    },
    GreaterThanEqualTo {
        override fun verifyCompare(result: Int) = result == 1 || result == 0
    },
    LessThanEqualTo {
        override fun verifyCompare(result: Int) = result == -1 || result == 0
    };

    abstract fun verifyCompare(result: Int): Boolean

}

interface Condition {
    fun evaluate(context: TemplateContext): Boolean
}

internal class LogicalCondition(
    val propertyFirst: DynamicProperty,
    val type: ConditionType,
    val propertySecond: DynamicProperty
) : Condition {
    override fun evaluate(context: TemplateContext): Boolean {
        return type.verifyCompare(propertyFirst.getValue(context)!!.compareAny(propertySecond.getValue(context)!!))
    }
}

internal class EvaluatedCondition(val value: Boolean) : Condition {
    override fun evaluate(context: TemplateContext): Boolean {
        return value
    }
}

enum class IfType(val order: Int) {
    If(0),
    ElseIf(1),
    Else(2)
}

internal class SingleIf(
    val condition: Condition,
    val type: IfType,
    val blockValue: LazyBlockSlice,
) : CodeGen {
    override fun generateTo(context: TemplateContext, stream: DestinationStream) {
        TODO("Not yet implemented")
    }
}


internal class IfStatement(private val ifs: MutableList<SingleIf>) : AtDirective {

    val singleIfs: List<SingleIf> get() = ifs

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

    override fun generateTo(context: TemplateContext, stream: DestinationStream) {
        evaluate(context)?.generateTo(context, stream)
    }
}