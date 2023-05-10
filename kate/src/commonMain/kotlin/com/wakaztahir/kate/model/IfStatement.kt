package com.wakaztahir.kate.model

import com.wakaztahir.kate.TemplateContext
import com.wakaztahir.kate.model.model.KATEObject
import com.wakaztahir.kate.model.model.KATEValue
import com.wakaztahir.kate.model.model.MutableKATEObject
import com.wakaztahir.kate.model.model.ReferencedOrDirectValue
import com.wakaztahir.kate.parser.stream.DestinationStream
import com.wakaztahir.kate.parser.stream.SourceStream
import com.wakaztahir.kate.tokenizer.NodeTokenizer


enum class ConditionType {
    ReferentiallyEquals {
        override fun compare(first: KATEValue, second: KATEValue): Boolean =
            first === second

        override fun verifyCompare(result: Int) = result == 0
    },
    Equals {
        override fun compare(first: KATEValue, second: KATEValue) =
            first.compareTo(second) == 0

        override fun verifyCompare(result: Int) = result == 0
    },
    NotEquals {
        override fun compare(first: KATEValue, second: KATEValue) =
            first.compareTo(second) != 0

        override fun verifyCompare(result: Int) = result != 0
    },
    GreaterThan {
        override fun compare(first: KATEValue, second: KATEValue) =
            first.compareTo(second) > 0

        override fun verifyCompare(result: Int) = result == 1
    },
    LessThan {
        override fun compare(first: KATEValue, second: KATEValue) =
            first.compareTo(second) < 0

        override fun verifyCompare(result: Int) = result == -1
    },
    GreaterThanEqualTo {
        override fun compare(first: KATEValue, second: KATEValue) =
            first.compareTo(second).let { it > 0 || it == 0 }

        override fun verifyCompare(result: Int) = result == 1 || result == 0
    },
    LessThanEqualTo {
        override fun compare(first: KATEValue, second: KATEValue) =
            first.compareTo(second).let { it < 0 || it == 0 }

        override fun verifyCompare(result: Int) = result == -1 || result == 0
    };

    abstract fun compare(first: KATEValue, second: KATEValue): Boolean
    abstract fun verifyCompare(result: Int): Boolean

}

interface Condition : ReferencedOrDirectValue {

    fun evaluate(context: KATEObject): Boolean

    override fun getKATEValue(model: KATEObject): KATEValue {
        return BooleanValue(evaluate(model))
    }

}

internal class LogicalCondition(
    val propertyFirst: ReferencedOrDirectValue,
    val type: ConditionType,
    val propertySecond: ReferencedOrDirectValue
) : Condition {
    override fun evaluate(context: KATEObject): Boolean {
        return type.compare(propertyFirst.getKATEValue(context), propertySecond.getKATEValue(context))
    }
}

enum class IfType(val order: Int) {
    If(0),
    ElseIf(1),
    Else(2)
}

class SingleIf(
    val condition: ReferencedOrDirectValue,
    val type: IfType,
    val blockValue: LazyBlockSlice,
) : CodeGen {
    override fun <T> selectNode(tokenizer: NodeTokenizer<T>): T = tokenizer.singleIf
    override fun generateTo(model: MutableKATEObject, destination: DestinationStream) {
        blockValue.generateTo(destination = destination)
    }
}


class IfStatement(private val ifs: MutableList<SingleIf>,val source: SourceStream) : BlockContainer {

    val singleIfs: List<SingleIf> get() = ifs

    override fun <T> selectNode(tokenizer: NodeTokenizer<T>): T = tokenizer.ifStatement

    private fun sortByOrder() {
        ifs.sortBy { it.type.order }
    }

    init {
        sortByOrder()
    }

    override fun getBlockValue(model: KATEObject): LazyBlock? {
        return evaluate(model)?.blockValue
    }

    fun evaluate(context: TemplateContext): SingleIf? {
        return evaluate(context.stream.model)
    }

    fun evaluate(context: KATEObject): SingleIf? {
        for (iffy in ifs) {
            if ((iffy.condition.asNullablePrimitive(context)!! as BooleanValue).value) {
                return iffy
            }
        }
        return null
    }

    override fun generateTo(model: MutableKATEObject, destination: DestinationStream) {
        evaluate(model)?.generateTo(model, destination)
        ifs.lastOrNull()?.blockValue?.blockEndPointer?.let { end ->
            source.setPointerAt(end)
        }
    }
}