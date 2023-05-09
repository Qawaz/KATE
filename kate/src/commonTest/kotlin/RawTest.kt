import com.wakaztahir.kate.TemplateContext
import com.wakaztahir.kate.parser.parseRawBlock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertNotEquals

class RawTest {

    @Test
    fun testRawBlock() {
        val context = TemplateContext("@raw there's something raw here @endraw")
        val block = context.stream.block.parseRawBlock()
        assertNotEquals(null, block)
        assertEquals("there's something raw here", block!!.value)
        context.updateStream("@rawvalue@endraw")
        context.stream.block.parseRawBlock()
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
    fun testRawIndentationInsidePartialRaw() {
        assertEquals(
            expected = "package output1",
            GenerateCode(
                "@partial_raw\n" +
                        "\t@raw\n" +
                        "\t\tpackage output1\n" +
                        "\t@endraw\n" +
                        "@end_partial_raw"
            )
        )
        assertEquals(
            expected = "package output2",
            GenerateCode(
                "@partial_raw\n" +
                        "    @raw\n" +
                        "        package output2\n" +
                        "    @endraw\n" +
                        "@end_partial_raw"
            )
        )
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
        assertFails {
            GenerateCode("@partial_raw BlockValue @end_partial_raw")
        }
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
                |${'\t'}@raw
                |${'\t'}${'\t'}package output
                |${'\t'}@endraw
                |@end_partial_raw
                """.trimMargin()
            )
        )
        assertEquals(
            expected = """	package output1
	package output2
	package output3
	package output4
	package output5
	package output6""",
            actual = GenerateCode(
                """@partial_raw
                |${'\t'}@raw
                |${'\t'}${'\t'}${'\t'}package output1
                |${'\t'}${'\t'}${'\t'}package output2
                |${'\t'}${'\t'}${'\t'}package output3
                |${'\t'}${'\t'}${'\t'}package output4
                |${'\t'}${'\t'}${'\t'}package output5
                |${'\t'}${'\t'}${'\t'}package output6
                |${'\t'}@endraw
                |@end_partial_raw
                """.trimMargin()
            )
        )
    }

}