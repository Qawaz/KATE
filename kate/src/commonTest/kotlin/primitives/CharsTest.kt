package primitives

import com.wakaztahir.kate.GenerateCode
import kotlin.test.Test
import kotlin.test.assertEquals

class CharsTest {

    @Test
    fun testCharacterEscape(){
        assertEquals("\b", GenerateCode("@var i = '\\b' @var(i)"))
        assertEquals("\n", GenerateCode("@var i = '\\n' @var(i)"))
        assertEquals("\r", GenerateCode("@var i = '\\r' @var(i)"))
        assertEquals("\t", GenerateCode("@var i = '\\t' @var(i)"))
        assertEquals("\\", GenerateCode("@var i = '\\\\' @var(i)"))
        assertEquals("'", GenerateCode("@var i = '\\'' @var(i)"))
        assertEquals("\"", GenerateCode("@var i = '\\\"' @var(i)"))
    }

}