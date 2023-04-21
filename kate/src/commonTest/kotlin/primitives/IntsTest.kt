package primitives

import GenerateCode
import com.wakaztahir.kate.TemplateContext
import com.wakaztahir.kate.parser.parseNumberValue
import com.wakaztahir.kate.parser.stream.TextDestinationStream
import kotlin.test.Test
import kotlin.test.assertEquals

class IntsTest {

    @Test
    fun testExactGeneration(){
        assertEquals("00345.000", GenerateCode("00345.000"))
        assertEquals("00345.", GenerateCode("00345."))
        assertEquals("00345", GenerateCode("00345"))
    }

}