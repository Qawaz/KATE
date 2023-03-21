package com.wakaztahir.kte.model

import com.wakaztahir.kte.TemplateContext
import com.wakaztahir.kte.model.model.MutableTemplateModel
import com.wakaztahir.kte.model.model.TemplateModel
import com.wakaztahir.kte.parser.BreakableForBlockParser
import com.wakaztahir.kte.parser.BreakableIfBlockParser
import com.wakaztahir.kte.parser.stream.DestinationStream
import com.wakaztahir.kte.parser.stream.SourceStream


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
    fun evaluate(context: TemplateModel): Boolean
    fun evaluate(context: TemplateContext): Boolean {
        return evaluate(context.stream.model)
    }
}

internal class LogicalCondition(
    val propertyFirst: ReferencedValue,
    val type: ConditionType,
    val propertySecond: ReferencedValue
) : Condition {
    override fun evaluate(context: TemplateModel): Boolean {
        val first = propertyFirst.getNullablePrimitive(context)
            ?: throw IllegalStateException("first referenced primitive value couldn't be found inside the condition")
        val second = propertySecond.getNullablePrimitive(context)
            ?: throw IllegalStateException("second referenced primitive value couldn't be found inside the condition")
        return type.verifyCompare(first.compareAny(second))
    }
}

internal class ReferencedBoolean(val value: ReferencedValue) : Condition {
    override fun evaluate(context: TemplateModel): Boolean {
        val value = value.getNullablePrimitive(context)
        if (value != null) {
            if (value is BooleanValue) {
                return value.value
            } else {
                throw IllegalStateException("referenced value is not a boolean value inside the condition")
            }
        } else {
            throw IllegalStateException("referenced value does not exist inside the condition")
        }
    }
}

internal class EvaluatedCondition(val value: Boolean) : Condition {
    override fun evaluate(context: TemplateModel): Boolean {
        return value
    }
}

enum class IfType(val order: Int) {
    If(0),
    ElseIf(1),
    Else(2)
}

internal class SingleIf(
    val parser: BreakableIfBlockParser,
    val condition: Condition,
    val type: IfType
) : CodeGen {
    override fun generateTo(model: MutableTemplateModel, source: SourceStream, destination: DestinationStream) {
        parser.generateTo(source = source, destination = destination)
    }
}


internal class IfStatement(private val ifs: MutableList<SingleIf>) : AtDirective {

    val singleIfs: List<SingleIf> get() = ifs

    private fun sortByOrder() {
        ifs.sortBy { it.type.order }
    }

    fun evaluate(context: TemplateContext): SingleIf? {
        return evaluate(context.stream.model)
    }

    fun evaluate(context: TemplateModel): SingleIf? {
        sortByOrder()
        for (iffy in ifs) {
            if (iffy.condition.evaluate(context)) {
                return iffy
            }
        }
        return null
    }

    override fun generateTo(model: MutableTemplateModel, source: SourceStream, destination: DestinationStream) {
        evaluate(model)?.generateTo(model = model, source = source, destination = destination)
    }
}