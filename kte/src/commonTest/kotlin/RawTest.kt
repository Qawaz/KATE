import com.wakaztahir.kte.GenerateCode
import com.wakaztahir.kte.KTEDelicateFunction
import com.wakaztahir.kte.TemplateContext
import com.wakaztahir.kte.parser.parseRawBlock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertNotEquals

class RawTest {

    @Test
    fun testRawBlock() {
        val context = TemplateContext("@raw there's something raw here @endraw")
        val block = context.stream.parseRawBlock()
        assertNotEquals(null, block)
        assertEquals("there's something raw here", block!!.value.getValueAsString())
        context.updateStream("@rawvalue@endraw")
        context.stream.parseRawBlock()
        assertEquals(16, context.stream.pointer)
    }

    @Test
    fun testRawCodeGen() {
        val text = "there's something raw here"
        assertEquals(text, GenerateCode("@raw $text @endraw"))
        assertEquals(text, GenerateCode("@raw${'\n'}$text${'\n'}@endraw"))
        assertEquals(" $text ", GenerateCode("@raw  $text  @endraw"))
    }

    @Test
    fun testTextGen() {
        var text = ""
        for (i in 0..127) text += i.toChar() + "\n"
        assertEquals(text, GenerateCode(text))
        var textInversed = ""
        for (i in 127 downTo 0) textInversed += i.toChar() + "\n"
        assertEquals(textInversed, GenerateCode(textInversed))
    }

    @Test
    fun testPartialRaw() {
        assertEquals("", GenerateCode("@partial_raw BlockValue @end_partial_raw"))
        assertFails {
            GenerateCode("@partial_raw @var i=5 @var(i) @end_partial_raw")
        }
        assertEquals(
            "Here",
            GenerateCode("@partial_raw @if(true) @default_no_raw Here @end_default_no_raw @endif @end_partial_raw")
        )
        assertEquals("Here", GenerateCode("@partial_raw @default_no_raw Here @end_default_no_raw @end_partial_raw"))
        assertEquals(
            "5",
            GenerateCode("@partial_raw @default_no_raw @var i=5 @var(i) @end_default_no_raw @end_partial_raw")
        )
        assertEquals(
            "Text @var i=5 @var(i)",
            GenerateCode("@partial_raw @raw Text @var i=5 @var(i) @endraw @end_partial_raw")
        )
        assertEquals(
            "@default_no_raw Text 5 @end_default_no_raw",
            GenerateCode("@default_no_raw Text @var i=5 @var(i) @end_default_no_raw")
        )
        assertEquals("5", GenerateCode("@partial_raw @var i = 5 @end_partial_raw @var(i)"))
        assertEquals(
            "5",
            GenerateCode("@partial_raw @default_no_raw @var i = 5 @end_default_no_raw @end_partial_raw @var(i)")
        )
    }

    @Test
    fun testPartialRaw2() {
        assertEquals(
            expected = """package output""",
            actual = GenerateCode(
                """@partial_raw
                |@raw
                |package output
                |@endraw
                |@end_partial_raw
                """.trimMargin()
            )
        )
    }

}