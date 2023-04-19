import com.wakaztahir.kate.TemplateContext
import com.wakaztahir.kate.parser.*
import com.wakaztahir.kate.parser.CommentParseException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class CommentsTest {

    @Test
    fun testComments() {
        val comment = "<%--This is my comment--%>"
        val context = TemplateContext(comment)
        assertEquals(true, context.stream.skipMultilineComments())
        assertEquals(comment.length, context.stream.pointer)
    }

    @Test
    fun testCommentWithoutEnding() {
        val context = TemplateContext("<%--This is my comment")
        assertFailsWith(CommentParseException::class) {
            context.stream.skipMultilineComments()
        }
    }

    @Test
    fun testNoComment() {
        val context = TemplateContext("There is no comment here")
        assertEquals(false, context.stream.skipMultilineComments())
    }

    @Test
    fun testCommentGeneration() {
        assertEquals("", GenerateCode("<%--I can type anything here--%>"))
    }

}