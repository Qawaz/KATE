import com.wakaztahir.kte.OutputStreamDestination
import com.wakaztahir.kte.TemplateContext
import com.wakaztahir.kte.model.model.TemplateModel
import com.wakaztahir.kte.model.model.MutableKTEObject
import com.wakaztahir.kte.parser.generateTo
import com.wakaztahir.kte.parser.stream.DestinationStream
import com.wakaztahir.kte.parser.stream.SourceStream
import com.wakaztahir.kte.parser.stream.TextSourceStream
import org.junit.Test
import java.io.File

class TestTemplates {

    private fun sourcePath(path: String, model: MutableKTEObject): SourceStream {
        return TextSourceStream(object {}.javaClass.getResource(path)!!.readText(), model)
    }

    private fun output(path: String): DestinationStream {
        val file = File("src/desktopTest/resources/$path")
        println(file.absolutePath)
        val outputStream = file.outputStream()
        return OutputStreamDestination(outputStream)
    }

    @Test
    fun testMainTemplate() {
        val context = TemplateContext(sourcePath("schema/main.kte", TemplateModel {
            putValue("mathTestClassName", "MathsClass")
            putObjects("arithmetic") {
                putObject {
                    putValue("funName", "seriouslyHere")
                    putValue("first", 4)
                    putValue("second", 6)
                    putValue("symbolName", "+")
                }
            }
        }))
        context.generateTo(output("output/main.kt"))
    }

}