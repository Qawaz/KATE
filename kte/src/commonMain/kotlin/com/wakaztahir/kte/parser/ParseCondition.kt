package com.wakaztahir.kte.parser

import com.wakaztahir.kte.TemplateContext
import com.wakaztahir.kte.parser.stream.SourceStream
import com.wakaztahir.kte.parser.stream.escapeSpaces
import com.wakaztahir.kte.parser.stream.increment
import com.wakaztahir.kte.parser.stream.printLeft

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

internal class Condition(
    val propertyFirst: PropertyOrValue,
    val type: ConditionType,
    val propertySecond: PropertyOrValue
) {
    fun evaluate(context: TemplateContext): Boolean {
        return type.verifyCompare(propertyFirst.getValue(context)!!.compareAny(propertySecond.getValue(context)!!))
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
        return null
    }

    stream.escapeSpaces()
    val type = stream.parseConditionType() ?: run {
        return null
    }

    stream.escapeSpaces()
    val propertySecond = stream.parseDynamicProperty() ?: run {
        throw IllegalStateException("condition's right hand side cannot be found")
    }

    return Condition(
        propertyFirst = propertyFirst,
        type = type,
        propertySecond = propertySecond
    )
}