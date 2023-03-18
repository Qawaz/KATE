import com.wakaztahir.kte.TemplateContext
import com.wakaztahir.kte.parser.*
import com.wakaztahir.kte.parser.CommentParseException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertFailsWith

class CommentsTest {

    @Test
    fun testComments() {
        val context = TemplateContext(TextStream("<%--This is my comment--%>"))
        assertEquals(true,context.stream.increment("<%--"))
    }

    @Test
    fun testCommentWithoutEnding() {
        val context = TemplateContext(TextStream("<%--This is my comment"))
        assertFailsWith(CommentParseException::class) {
            context.parseComment()
        }
    }

    @Test
    fun testNoComment() {
        val context = TemplateContext(TextStream("There is no comment here"))
        assertEquals(false,context.parseComment())
    }

}