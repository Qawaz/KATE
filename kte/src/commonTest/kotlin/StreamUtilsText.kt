import com.wakaztahir.kte.TemplateContext
import com.wakaztahir.kte.parser.stream.*
import com.wakaztahir.kte.parser.stream.increment
import kotlin.test.*

class StreamUtilsText {
    @Test
    fun testIncrement() {
        val context = TemplateContext(TextStream("<%--HelloWorld--%>"))
        assertTrue(context.stream.increment("<%--"))
        assertEquals(4, context.stream.pointer)
        assertFalse(context.stream.increment("<%--"))
        assertEquals(4, context.stream.pointer)
        assertTrue(context.stream.increment("HelloWorld"))
        assertEquals(14, context.stream.pointer)
        assertTrue(context.stream.increment("--%>"))
        assertFailsWith(UnexpectedEndOfStream::class) {
            context.stream.increment("any", throwOnUnexpectedEOS = true)
        }
    }

    @Test
    fun testIncrementUntil() {
        val context = TemplateContext(TextStream("<%--HelloWorld--%>"))
        assertEquals(true, context.stream.increment("<%--"))
        assertEquals(true, context.stream.incrementUntil("llo"))
        assertEquals(9,context.stream.pointer)
        assertEquals(false, context.stream.incrementUntil("..."))
        assertEquals(9,context.stream.pointer)
        assertEquals(true, context.stream.incrementUntil("--%>"))
    }

    @Test
    fun testParseTextUntil() {
        val context = TemplateContext(TextStream("<%--This is my comment--%>"))
        assertEquals(true, context.stream.increment("<%--"))
        assertEquals("This is my comment", context.stream.parseTextUntilConsumed("--%>"))
    }

    @Test
    fun testParseTextUntilChar() {
        val context = TemplateContext(TextStream("<%--This is my comment--%>"))
        assertEquals(true, context.stream.increment("<%--"))
        assertEquals("Th", context.stream.parseTextUntil('i'))
        assertEquals("is is ", context.stream.parseTextUntil('m'))
        assertEquals("my comment", context.stream.parseTextUntil('-'))
    }

}