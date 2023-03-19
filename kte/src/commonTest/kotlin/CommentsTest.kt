import com.wakaztahir.kte.TemplateContext
import com.wakaztahir.kte.parser.*
import com.wakaztahir.kte.parser.CommentParseException
import com.wakaztahir.kte.parser.stream.TextStream
import com.wakaztahir.kte.parser.stream.increment
import kotlin.test.Test
import kotlin.test.assertEquals
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
            context.stream.parseComment()
        }
    }

    @Test
    fun testNoComment() {
        val context = TemplateContext(TextStream("There is no comment here"))
        assertEquals(false,context.stream.parseComment())
    }

}