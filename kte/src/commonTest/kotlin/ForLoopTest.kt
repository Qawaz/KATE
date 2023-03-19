import com.wakaztahir.kte.TemplateContext
import com.wakaztahir.kte.model.ModelDirective
import com.wakaztahir.kte.parser.ArithmeticOperatorType
import com.wakaztahir.kte.parser.ForLoop
import com.wakaztahir.kte.parser.parseForLoop
import com.wakaztahir.kte.parser.stream.TextStream
import kotlin.test.Test
import kotlin.test.assertEquals

class ForLoopTest {
    @Test
    fun parseForLoop() {
        val context = TemplateContext("@for(true) blockValue @endfor")
        val loop = context.stream.parseForLoop()!! as ForLoop.ConditionalFor
        assertEquals("blockValue", loop.blockValue.getValueAsString(context.stream))
    }

    @Test
    fun parseForLoopIterable() {
        val context = TemplateContext("@for(@const listName : @model.mList) blockValue @endfor")
        var loop = context.stream.parseForLoop()!! as ForLoop.IterableFor
        assertEquals("listName", loop.elementConstName)
        assertEquals(null, loop.indexConstName)
        assertEquals("blockValue", loop.blockValue.getValueAsString(context.stream))
        assertEquals("mList", (loop.listProperty as ModelDirective).propertyPath[0].name)
        context.updateStream(TextStream("@for(@const listName,indexName : @model.list) blockValue @endfor"))
        loop = context.stream.parseForLoop()!! as ForLoop.IterableFor
        assertEquals("indexName", loop.indexConstName)
    }

    @Test
    fun parseForLoopNumbered() {
        val operatorType = ArithmeticOperatorType.Plus
        val context = TemplateContext("@for(@const i=0;i<5;i${operatorType.char}1) blockValue @endfor")
        val loop = context.stream.parseForLoop()!! as ForLoop.NumberedFor
        assertEquals("i", loop.variableName)
        assertEquals(0, loop.initializer.getStoredValue()!!.value!!)
        assertEquals(5, loop.conditional.getStoredValue()!!.value!!)
        assertEquals(operatorType, loop.arithmeticOperatorType)
        assertEquals(1, loop.incrementer.getStoredValue()!!.value!!)
        assertEquals("blockValue", loop.blockValue.getValueAsString(context.stream))
    }


}