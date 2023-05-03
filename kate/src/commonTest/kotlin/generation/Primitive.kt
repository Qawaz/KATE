package generation

import GenerateCode
import kotlin.test.Test
import kotlin.test.assertEquals

class Primitive {

    @Test
    fun testNumbers() {
        assertEquals("00345.000", GenerateCode("00345.000"))
        assertEquals("00345.", GenerateCode("00345."))
        assertEquals("00345", GenerateCode("00345"))
    }

    @Test
    fun testBooleans() {
        assertEquals("true", GenerateCode("true"))
        assertEquals("false", GenerateCode("false"))
        assertEquals("true && true", GenerateCode("true && true"))
        assertEquals("true && false", GenerateCode("true && false"))
        assertEquals("!true", GenerateCode("!true"))
        assertEquals("!false", GenerateCode("!false"))
    }

}