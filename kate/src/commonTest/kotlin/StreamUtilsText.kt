import com.wakaztahir.kate.TemplateContext
import com.wakaztahir.kate.lexer.tokens.StaticTokens
import com.wakaztahir.kate.parser.stream.*
import com.wakaztahir.kate.parser.stream.increment
import kotlin.test.*

class StreamUtilsText {
    @Test
    fun testIncrement() {
        val context = TemplateContext(("<%--HelloWorld--%>"))
        assertTrue(context.stream.increment(StaticTokens.CommentStart))
        assertEquals(4, context.stream.pointer)
        assertFalse(context.stream.increment(StaticTokens.CommentStart))
        assertEquals(4, context.stream.pointer)
        assertTrue(context.stream.increment("HelloWorl"))
        assertEquals(13, context.stream.pointer)
        assertTrue(context.stream.increment('d'))
        assertEquals(14, context.stream.pointer)
        assertTrue(context.stream.increment(StaticTokens.CommentEnd))
        assertFailsWith(UnexpectedEndOfStream::class) {
            context.stream.increment("any", throwOnUnexpectedEOS = true)
        }
    }

    @Test
    fun testIncrementUntil() {
        val comment = "<%--HelloWorld--%>"
        val context = TemplateContext(comment)
        assertEquals(true, context.stream.increment(StaticTokens.CommentStart))
        assertEquals(true, context.stream.incrementUntil("llo"))
        assertEquals(6, context.stream.pointer)
        assertEquals(true, context.stream.incrementUntilConsumed("llo"))
        assertEquals(9, context.stream.pointer)
        assertEquals(false, context.stream.incrementUntil("..."))
        assertEquals(9, context.stream.pointer)
        assertEquals(true, context.stream.incrementUntil(StaticTokens.CommentEnd))
        assertEquals(14, context.stream.pointer)
        assertEquals(true, context.stream.incrementUntilConsumed(StaticTokens.CommentEnd))
        assertEquals(comment.length,context.stream.pointer)
    }

    @Test
    fun testParseTextUntil() {
        val context = TemplateContext(("<%--This is my comment--%>"))
        assertEquals(true, context.stream.increment(StaticTokens.CommentStart))
        assertEquals("This is my comment", context.stream.parseTextUntilConsumed(StaticTokens.CommentEnd))
    }

    @Test
    fun testParseTextUntilChar() {
        val context = TemplateContext(("<%--This is my comment--%>"))
        assertEquals(true, context.stream.increment(StaticTokens.CommentStart))
        assertEquals("Th", context.stream.parseTextWhile { currentChar != 'i' })
        assertEquals("is is ", context.stream.parseTextWhile { currentChar != 'm' })
        assertEquals("my comment", context.stream.parseTextWhile { currentChar != '-' })
    }

}