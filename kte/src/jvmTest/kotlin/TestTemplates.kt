import com.wakaztahir.kte.InputSourceStream
import com.wakaztahir.kte.OutputDestinationStream
import com.wakaztahir.kte.TemplateContext
import com.wakaztahir.kte.model.model.MutableKTEObject
import com.wakaztahir.kte.parser.stream.SourceStream
import org.junit.Test
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class TestTemplates {

    private fun getObject(): MutableKTEObject {
        return MutableKTEObject {
            putValue("mathTestClassName", "MathsClass")
            putObjects("arithmetic") {
                putObject {
                    putValue("funName", "sumsTwoVars")
                    putValue("first", 4)
                    putValue("second", 6)
                    putValue("symbolName", "+")
                    putValue("returnType", "Int")
                }
                putObject {
                    putValue("funName", "subtractTwoVars")
                    putValue("first", 6)
                    putValue("second", 4)
                    putValue("symbolName", "-")
                    putValue("returnType", "Int")
                }
                putObject {
                    putValue("funName", "multiplyTwoVars")
                    putValue("first", 4)
                    putValue("second", 6)
                    putValue("symbolName", "*")
                    putValue("returnType", "Int")
                }
                putObject {
                    putValue("funName", "divideTwoVars")
                    putValue("first", 4)
                    putValue("second", 2)
                    putValue("symbolName", "/")
                    putValue("returnType", "Int")
                }
            }
        }
    }

    private fun sourcePath(path: String, model: MutableKTEObject): SourceStream {
        return InputSourceStream(object {}.javaClass.getResource(path)!!.openStream(), model)
    }

    private fun output(path: String): OutputDestinationStream {
        val file = File("src/jvmTest/resources/$path")
        println(file.absolutePath)
        val outputStream = file.outputStream()
        return OutputDestinationStream(outputStream)
    }

    @Test
    fun testInputSourceStream() {

        val input = object {}.javaClass.getResource("schema/main.kte")!!.openStream()
        val reader = input.bufferedReader()
        val text = reader.readText()
        reader.close()

        val sourceStream = InputSourceStream(object {}.javaClass.getResource("schema/main.kte")!!.openStream(), getObject())
        assertFalse(sourceStream.hasEnded)
        val other = sourceStream.getValueAsString(0)
        assertEquals(text, other)
    }

    @Test
    fun testMainTemplate() {
        val context = TemplateContext(sourcePath("schema/main.kte", getObject()))
        val output = output("output/main.kt")
        context.generateTo(output)
        output.outputStream.close()
    }

}