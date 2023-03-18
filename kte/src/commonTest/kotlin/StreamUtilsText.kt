import com.wakaztahir.kte.TemplateContext
import com.wakaztahir.kte.parser.*
import com.wakaztahir.kte.parser.increment
import com.wakaztahir.kte.parser.incrementUntil
import com.wakaztahir.kte.parser.parseTextUntil
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
        val context = TemplateContext(TextStream("<%--This is my comment--%>"))
        assertEquals(true, context.stream.increment("<%--"))
        assertEquals(true, context.stream.incrementUntil("--%>"))
        assertEquals(false, context.stream.increment("<%--"))
        assertEquals(false, context.stream.incrementUntil("--%>"))
    }

    @Test
    fun testParseTextUntil() {
        val context = TemplateContext(TextStream("<%--This is my comment--%>"))
        assertEquals(true, context.stream.increment("<%--"))
        assertEquals("This is my comment", context.stream.parseTextUntil("--%>"))
    }
}