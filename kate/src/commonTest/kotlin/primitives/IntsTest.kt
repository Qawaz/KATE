package primitives

import GenerateCode
import com.wakaztahir.kate.TemplateContext
import com.wakaztahir.kate.parser.parseNumberValue
import com.wakaztahir.kate.parser.stream.TextDestinationStream
import kotlin.test.Test
import kotlin.test.assertEquals

class IntsTest {

    @Test
    fun testZeroPrefixedNumber() {
        val context = TemplateContext("00345")
        val number = context.stream.parseNumberValue()!!
        assertEquals("00345", number.toString())
        val invocation = number.toPlaceholderInvocation(context.stream.model, context.stream.pointer)!!
        val generated = TextDestinationStream().also { invocation.generateTo(context.stream, it) }
        assertEquals("00345", generated.getValue())
        assertEquals("00345", GenerateCode("00345"))
    }

}