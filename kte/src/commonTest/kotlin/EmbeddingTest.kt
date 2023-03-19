import com.wakaztahir.kte.TemplateContext
import com.wakaztahir.kte.parser.parseConstantDeclaration
import com.wakaztahir.kte.parser.parseConstantReference
import com.wakaztahir.kte.parser.parseEmbedding
import com.wakaztahir.kte.parser.stream.TextStream
import kotlin.test.Test
import kotlin.test.assertEquals

class EmbeddingTest {
    @Test
    fun testParseEmbeddingPath() {
        val context = TemplateContext("@embed ./current.kte")
        val path = context.stream.parseEmbedding()!!
        assertEquals("./current.kte", path)
        context.embedStream(path, TextStream("@const var1 = \"hello-world\""))
        val ref = context.getEmbeddedStream(path)!!.parseConstantDeclaration()!!
        assertEquals("var1", ref.variableName)
        assertEquals("hello-world", ref.variableValue.getStoredValue()!!.value)
    }
}