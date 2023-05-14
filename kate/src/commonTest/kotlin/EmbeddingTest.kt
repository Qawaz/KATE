import com.wakaztahir.kate.TemplateContext
import com.wakaztahir.kate.model.LazyBlock
import com.wakaztahir.kate.model.asPrimitive
import com.wakaztahir.kate.parser.parseEmbedding
import com.wakaztahir.kate.parser.stream.EmbeddingManager
import com.wakaztahir.kate.parser.stream.SourceStream
import com.wakaztahir.kate.parser.stream.TextSourceStream
import com.wakaztahir.kate.parser.variable.VariableDeclaration
import com.wakaztahir.kate.parser.variable.parseVariableDeclaration
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class EmbeddingTest {

    @Test
    fun testParseEmbeddingPath() {
        val context = TemplateContext(TextSourceStream("@embed ./current.kte", embeddingManager = object : EmbeddingManager {
            override val embeddedStreams: MutableMap<String, Boolean> = mutableMapOf()
            override fun provideStream(block: LazyBlock, path: String): SourceStream? {
                return if(path == "./current.kte") {
                    TextSourceStream("@var var1 = \"hello-world\"")
                } else {
                    null
                }
            }
        }))
        val embedding = context.stream.block.parseEmbedding()!!
        val path = embedding.path
        assertNotEquals(null,embedding.parsedBlock)
        val declaration = (embedding.parsedBlock!!.codeGens[0].gen as VariableDeclaration)
        assertEquals("./current.kte", path)
        assertEquals("var1", declaration.variableName)
        assertEquals("hello-world", declaration.variableValue.asPrimitive().value)
    }

    @Test
    fun testEmbeddingVariable(){
        val context = TemplateContext(TextSourceStream("@embed_once ./file.kate\n@var(i)", embeddingManager = object : EmbeddingManager {
            override val embeddedStreams: MutableMap<String, Boolean> = mutableMapOf()
            override fun provideStream(block: LazyBlock, path: String): SourceStream {
                return TextSourceStream(model = block.model,sourceCode = "@var i = 5")
            }
        }))
        assertEquals("\n5",context.getDestinationAsString())
    }

}