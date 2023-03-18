package com.wakaztahir.kte.model

import com.wakaztahir.kte.TemplateContext
import com.wakaztahir.kte.parser.ConditionType
import com.wakaztahir.kte.parser.PropertyOrValue

interface Condition {
    fun evaluate(context: TemplateContext): Boolean
}

internal class LogicalCondition(
    val propertyFirst: PropertyOrValue,
    val type: ConditionType,
    val propertySecond: PropertyOrValue
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