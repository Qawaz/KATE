package com.wakaztahir.kte.model

import com.wakaztahir.kte.TemplateContext

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