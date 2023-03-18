package com.wakaztahir.kte.parser

import com.wakaztahir.kte.TemplateContext
import com.wakaztahir.kte.parser.stream.SourceStream
import com.wakaztahir.kte.parser.stream.escapeSpaces
import com.wakaztahir.kte.parser.stream.increment
import com.wakaztahir.kte.parser.stream.printLeft

internal sealed class ConditionType {

    abstract fun evaluate(context: TemplateContext, valueOne: DynamicProperty, valueTwo: DynamicProperty): Boolean

    object Equals : ConditionType() {
        override fun evaluate(context: TemplateContext, valueOne: DynamicProperty, valueTwo: DynamicProperty): Boolean {
            val first = valueOne.getValue(context)
            val second = valueTwo.getValue(context)
            return first == second
        }
    }

    object NotEquals : ConditionType(){
        override fun evaluate(context: TemplateContext, valueOne: DynamicProperty, valueTwo: DynamicProperty): Boolean {
            val first = valueOne.getValue(context)
            val second = valueTwo.getValue(context)
            return first != second
        }
    }
    object GreaterThan : ConditionType(){
        override fun evaluate(context: TemplateContext, valueOne: DynamicProperty, valueTwo: DynamicProperty): Boolean {
            TODO("Not yet implemented")
        }
    }
    object LessThan : ConditionType(){
        override fun evaluate(context: TemplateContext, valueOne: DynamicProperty, valueTwo: DynamicProperty): Boolean {
            TODO("Not yet implemented")
        }
    }
    object GreaterThanEqualTo : ConditionType(){
        override fun evaluate(context: TemplateContext, valueOne: DynamicProperty, valueTwo: DynamicProperty): Boolean {
            TODO("Not yet implemented")
        }
    }
    object LessThanEqualTo : ConditionType(){
        override fun evaluate(context: TemplateContext, valueOne: DynamicProperty, valueTwo: DynamicProperty): Boolean {
            TODO("Not yet implemented")
        }
    }

}

internal class Condition(
    val propertyFirst: DynamicProperty,
    val type: ConditionType,
    val propertySecond: DynamicProperty
) {
    fun evaluate(context: TemplateContext): Boolean {
        return type.evaluate(context,propertyFirst,propertySecond)
    }
}

internal fun SourceStream.parseConditionType(): ConditionType? {
    if (increment("==")) {
        return ConditionType.Equals
    } else if (increment("!=")) {
        return ConditionType.NotEquals
    } else if (increment(">")) {
        return if (increment("=")) {
            ConditionType.GreaterThanEqualTo
        } else {
            ConditionType.GreaterThan
        }
    } else if (increment("<")) {
        return if (increment("=")) {
            ConditionType.LessThanEqualTo
        } else {
            ConditionType.LessThan
        }
    } else {
        return null
    }
}

internal fun TemplateContext.parseCondition(): Condition? {
    val propertyFirst = stream.parseDynamicProperty() ?: run {
        stream.printLeft()
        return null
    }
    stream.escapeSpaces()
    val type = stream.parseConditionType() ?: run {
        stream.printLeft()
        return null
    }
    val propertySecond = stream.parseDynamicProperty() ?: run {
        throw IllegalStateException("condition's right hand side cannot be found")
    }
    return Condition(
        propertyFirst = propertyFirst,
        type = type,
        propertySecond = propertySecond
    )
}