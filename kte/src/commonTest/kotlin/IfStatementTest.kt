import com.wakaztahir.kte.TemplateContext
import com.wakaztahir.kte.parser.parseCondition
import com.wakaztahir.kte.parser.stream.TextStream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class IfStatementTest {

    @Test
    fun testUnequalCondition() {
        val context = TemplateContext(TextStream("\"ValueOne\" == \"SecondValue\""))
        val condition = context.parseCondition()!!
        assertEquals("ValueOne", condition.propertyFirst.getValue(context))
        assertEquals("SecondValue", condition.propertySecond.getValue(context))
        assertFalse(condition.evaluate(context))
    }

    @Test
    fun testEqualCondition() {
        val context = TemplateContext(TextStream("\"SecondValue\" == \"SecondValue\""))
        val condition = context.parseCondition()!!
        assertTrue(condition.evaluate(context))
    }

}