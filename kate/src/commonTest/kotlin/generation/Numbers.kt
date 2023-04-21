package generation

import GenerateCode
import kotlin.test.Test
import kotlin.test.assertEquals

class Numbers {
    @Test
    fun testExactGeneration() {
        assertEquals("00345.000", GenerateCode("00345.000"))
        assertEquals("00345.", GenerateCode("00345."))
        assertEquals("00345", GenerateCode("00345"))
    }
}