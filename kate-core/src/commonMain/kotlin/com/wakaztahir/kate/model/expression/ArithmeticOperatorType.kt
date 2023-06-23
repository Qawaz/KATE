package com.wakaztahir.kate.model.expression;

enum class ArithmeticOperatorType(val char: String, val associativity: OperatorAssociativity, val precedence: Int) {

    Plus("+", associativity = OperatorAssociativity.LeftToRight, precedence = 6) {
        override fun operate(value1: Int, value2: Int): Int = value1 + value2
        override fun operate(value1: Double, value2: Double): Double = value1 + value2
        override fun operate(value1: Int, value2: Double): Double = value1 + value2
        override fun operate(value1: Int, value2: Long): Long = value1 + value2
        override fun operate(value1: Double, value2: Int): Double = value1 + value2
        override fun operate(value1: Double, value2: Long): Double = value1 + value2
        override fun operate(value1: Long, value2: Long): Long = value1 + value2
        override fun operate(value1: Long, value2: Int): Long = value1 + value2
        override fun operate(value1: Long, value2: Double): Double = value1 + value2
        override fun operate(value1: String, value2: String): String = value1 + value2
        override fun operate(value1: String, value2: Int): String = value1 + value2
        override fun operate(value1: String, value2: Double): String = value1 + value2
        override fun operate(value1: String, value2: Char): String = value1 + value2
        override fun operate(value1: Char, value2: Int): Char = value1 + value2
        override fun operate(value1: Char, value2: String): String = value1 + value2

    },
    Minus("-", associativity = OperatorAssociativity.LeftToRight, precedence = 6) {
        override fun operate(value1: Int, value2: Int): Int = value1 - value2
        override fun operate(value1: Double, value2: Double): Double = value1 - value2
        override fun operate(value1: Int, value2: Double): Double = value1 - value2
        override fun operate(value1: Int, value2: Long): Long = value1 - value2
        override fun operate(value1: Double, value2: Int): Double = value1 - value2
        override fun operate(value1: Double, value2: Long): Double = value1 - value2
        override fun operate(value1: Long, value2: Long): Long = value1 - value2
        override fun operate(value1: Long, value2: Int): Long = value1 - value2
        override fun operate(value1: Long, value2: Double): Double = value1 - value2
        override fun operate(value1: Char, value2: Char): Int = value1 - value2
    },
    Divide("/", associativity = OperatorAssociativity.LeftToRight, precedence = 4) {
        override fun operate(value1: Int, value2: Int): Int = value1 / value2
        override fun operate(value1: Double, value2: Double): Double = value1 / value2
        override fun operate(value1: Int, value2: Double): Double = value1 / value2
        override fun operate(value1: Int, value2: Long): Long = value1 / value2
        override fun operate(value1: Double, value2: Int): Double = value1 / value2
        override fun operate(value1: Double, value2: Long): Double = value1 / value2
        override fun operate(value1: Long, value2: Long): Long = value1 / value2
        override fun operate(value1: Long, value2: Int): Long = value1 / value2
        override fun operate(value1: Long, value2: Double): Double = value1 / value2
    },
    Multiply("*", associativity = OperatorAssociativity.LeftToRight, precedence = 4) {
        override fun operate(value1: Int, value2: Int): Int = value1 * value2
        override fun operate(value1: Double, value2: Double): Double = value1 * value2
        override fun operate(value1: Int, value2: Double): Double = value1 * value2
        override fun operate(value1: Int, value2: Long): Long = value1 * value2
        override fun operate(value1: Double, value2: Int): Double = value1 * value2
        override fun operate(value1: Double, value2: Long): Double = value1 * value2
        override fun operate(value1: Long, value2: Long): Long = value1 * value2
        override fun operate(value1: Long, value2: Int): Long = value1 * value2
        override fun operate(value1: Long, value2: Double): Double = value1 * value2
    },
    Mod("%", associativity = OperatorAssociativity.LeftToRight, precedence = 4) {
        override fun operate(value1: Int, value2: Int): Int = value1 % value2
        override fun operate(value1: Double, value2: Double): Double = value1 % value2
        override fun operate(value1: Int, value2: Double): Double = value1 % value2
        override fun operate(value1: Int, value2: Long): Long = value1 % value2
        override fun operate(value1: Double, value2: Int): Double = value1 % value2
        override fun operate(value1: Double, value2: Long): Double = value1 % value2
        override fun operate(value1: Long, value2: Long): Long = value1 % value2
        override fun operate(value1: Long, value2: Int): Long = value1 % value2
        override fun operate(value1: Long, value2: Double): Double = value1 % value2
    },
    And("&&", associativity = OperatorAssociativity.LeftToRight, precedence = 2) {
        override fun operate(value1: Boolean, value2: Boolean): Boolean = value1 && value2
    },
    Or("||", associativity = OperatorAssociativity.LeftToRight, precedence = 1) {
        override fun operate(value1: Boolean, value2: Boolean): Boolean = value1 || value2
    },
    Equal("==", associativity = OperatorAssociativity.LeftToRight, precedence = 3) {
//        override fun operate(value1: KATEValue, value2: KATEValue): KATEValue =
//            BooleanValue(ConditionType.Equals.verifyCompare(value1.compareTo(value2)))
    },
    NotEqual("!=", associativity = OperatorAssociativity.LeftToRight, precedence = 3) {
//        override fun operate(value1: KATEValue, value2: KATEValue): KATEValue =
//            BooleanValue(ConditionType.NotEquals.verifyCompare(value1.compareTo(value2)))
    },
    LessThanOrEqual("<=", associativity = OperatorAssociativity.LeftToRight, precedence = 5) {
//        override fun operate(value1: KATEValue, value2: KATEValue): KATEValue =
//            BooleanValue(ConditionType.LessThanEqualTo.verifyCompare(value1.compareTo(value2)))
    },
    LessThan("<", associativity = OperatorAssociativity.LeftToRight, precedence = 5) {
//        override fun operate(value1: KATEValue, value2: KATEValue): KATEValue =
//            BooleanValue(ConditionType.LessThan.verifyCompare(value1.compareTo(value2)))
    },
    GreaterThan(">", associativity = OperatorAssociativity.LeftToRight, precedence = 5) {
//        override fun operate(value1: KATEValue, value2: KATEValue): KATEValue =
//            BooleanValue(ConditionType.GreaterThan.verifyCompare(value1.compareTo(value2)))
    },
    GreaterThanOrEqual(">=", associativity = OperatorAssociativity.LeftToRight, precedence = 5) {
//        override fun operate(value1: KATEValue, value2: KATEValue): KATEValue =
//            BooleanValue(ConditionType.GreaterThanEqualTo.verifyCompare(value1.compareTo(value2)))
    },
    ReferentialEquality("===", associativity = OperatorAssociativity.LeftToRight, precedence = 3) {
//        override fun operate(value1: KATEValue, value2: KATEValue): KATEValue =
//            BooleanValue(ConditionType.ReferentiallyEquals.verifyCompare(value1.compareTo(value2)))
    };

    private fun notPossible(value1: Any, value2: Any): Nothing {
        throw IllegalStateException("operation : $char is not possible between $value1 and $value2")
    }

    open fun operate(value1: Boolean, value2: Boolean): Boolean = notPossible(value1, value2)
    open fun operate(value1: Int, value2: Int): Int = notPossible(value1, value2)
    open fun operate(value1: Int, value2: Double): Double = notPossible(value1, value2)
    open fun operate(value1: Int, value2: Long): Long = notPossible(value1, value2)
    open fun operate(value1: Double, value2: Double): Double = notPossible(value1, value2)
    open fun operate(value1: Double, value2: Int): Double = notPossible(value1, value2)
    open fun operate(value1: Double, value2: Long): Double = notPossible(value1, value2)
    open fun operate(value1: Long, value2: Long): Long = notPossible(value1, value2)
    open fun operate(value1: Long, value2: Int): Long = notPossible(value1, value2)
    open fun operate(value1: Long, value2: Double): Double = notPossible(value1, value2)
    open fun operate(value1: String, value2: String): String = notPossible(value1, value2)
    open fun operate(value1: String, value2: Int): String = notPossible(value1, value2)
    open fun operate(value1: String, value2: Double): String = notPossible(value1, value2)
    open fun operate(value1: String, value2: Char): String = notPossible(value1, value2)
    open fun operate(value1: Char, value2: Char): Int = notPossible(value1, value2)
    open fun operate(value1: Char, value2: Int): Char = notPossible(value1, value2)
    open fun operate(value1: Char, value2: String): String = notPossible(value1, value2)
//    open fun operate(value1: KATEValue, value2: KATEValue): KATEValue = notPossible(value1, value2)

}
