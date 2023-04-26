package variables

import GenerateCode
import kotlin.test.Test
import kotlin.test.assertEquals

class ConditionTest {

    @Test
    fun testConditionAsVariableValue() {
        assertEquals("true", GenerateCode("@var x = true == true @var(x)"))
        assertEquals("false", GenerateCode("@var x = true == false @var(x)"))
        assertEquals("true", GenerateCode("@var x = false == false @var(x)"))
        assertEquals("false", GenerateCode("@var x = false == true @var(x)"))
        assertEquals("false", GenerateCode("@var x = false != false @var(x)"))
        assertEquals("true", GenerateCode("@var x = false != true @var(x)"))
    }
//
//    @Test
//    fun testConditionalVariableTest() {
//        assertEquals("true", GenerateCode("@var j = true @var k = true @var x = j == k @var(x)"))
//        assertEquals("false", GenerateCode("@var j = true @var k = false @var x = j == k @var(x)"))
//        assertEquals("true", GenerateCode("@var j = false @var k = false @var x = j == k @var(x)"))
//        assertEquals("false", GenerateCode("@var j = false @var k = true @var x = j == k @var(x)"))
//        assertEquals("false", GenerateCode("@var j = false @var k = false @var x = j != k @var(x)"))
//        assertEquals("true", GenerateCode("@var j = false @var k = true @var x = j != k @var(x)"))
//    }
//
//    @Test
//    fun numberedCondition() {
//        assertEquals("true", GenerateCode("@var x = 6 > 5 @var(x)"))
//        assertEquals("true", GenerateCode("@var j = 20 @var k = 10 @var x = j > k @var(x)"))
//    }

}