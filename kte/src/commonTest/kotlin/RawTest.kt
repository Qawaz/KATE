import com.wakaztahir.kte.GenerateCode
import com.wakaztahir.kte.KTEDelicateFunction
import com.wakaztahir.kte.TemplateContext
import com.wakaztahir.kte.parser.parseRawBlock
import kotlin.test.Test
import kotlin.test.assertEquals
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

}