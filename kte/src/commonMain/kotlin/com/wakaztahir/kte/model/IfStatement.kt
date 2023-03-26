package com.wakaztahir.kte.model

import com.wakaztahir.kte.TemplateContext
import com.wakaztahir.kte.model.model.KTEObject
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
    fun evaluate(context: KTEObject): Boolean
    fun evaluate(context: TemplateContext): Boolean {
        return evaluate(context.stream.model)
    }
}

internal class LogicalCondition(
    val propertyFirst: ReferencedValue,
    val type: ConditionType,
    val propertySecond: ReferencedValue
) : Condition {
    override fun evaluate(context: KTEObject): Boolean {
        propertyFirst.asNullablePrimitive(context)?.let { first ->
            propertySecond.asNullablePrimitive(context)?.let { second ->
                return type.verifyCompare(first.compareAny(second))
            } ?: run {
                throw IllegalStateException("second value in condition $this is not a primitive")
            }
        } ?: run {
            throw IllegalStateException("first value in condition $this is not a primitive")
        }
    }
}

internal class ReferencedBoolean(val value: ReferencedValue) : Condition {
    override fun evaluate(context: KTEObject): Boolean {
        val value = value.asNullablePrimitive(context)
        if (value != null && value is BooleanValue) {
            return value.value
        } else {
            throw IllegalStateException("referenced value is not a boolean value inside the conditions")
        }
    }
}

internal class EvaluatedCondition(val value: Boolean) : Condition {
    override fun evaluate(context: KTEObject): Boolean {
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
    override fun generateTo(block: LazyBlock, destination: DestinationStream) {
        blockValue.generateTo(destination = destination)
    }
}


internal class IfStatement(private val ifs: MutableList<SingleIf>) : BlockContainer {

    val singleIfs: List<SingleIf> get() = ifs

    private fun sortByOrder() {
        ifs.sortBy { it.type.order }
    }

    init {
        sortByOrder()
    }

    override fun getBlockValue(model: KTEObject): LazyBlock? {
        return evaluate(model)?.blockValue
    }

    fun evaluate(context: TemplateContext): SingleIf? {
        return evaluate(context.stream.model)
    }

    fun evaluate(context: KTEObject): SingleIf? {
        for (iffy in ifs) {
            if (iffy.condition.evaluate(context)) {
                return iffy
            }
        }
        return null
    }

    override fun generateTo(block: LazyBlock, destination: DestinationStream) {
        evaluate(block.model)?.generateTo(block, destination)
        ifs.lastOrNull()?.blockValue?.blockEndPointer?.let { end ->
            block.source.setPointerAt(end)
        }
    }
}