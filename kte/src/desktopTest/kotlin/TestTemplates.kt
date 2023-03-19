import com.wakaztahir.kte.InputStreamSource
import com.wakaztahir.kte.OutputStreamDestination
import com.wakaztahir.kte.TemplateContext
import com.wakaztahir.kte.parser.generateTo
import com.wakaztahir.kte.parser.parse
import com.wakaztahir.kte.parser.stream.DestinationStream
import com.wakaztahir.kte.parser.stream.SourceStream
import com.wakaztahir.kte.parser.stream.TextSourceStream
import org.junit.Test
import java.io.File
import java.net.URI
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.io.path.createFile
import kotlin.io.path.outputStream
import kotlin.io.path.toPath

class TestTemplates {

    private fun sourcePath(path: String): SourceStream {
        return TextSourceStream(object {}.javaClass.getResource(path)!!.readText())
    }

    private fun output(path: String): DestinationStream {
        val file = File("src/desktopTest/resources/$path")
        println(file.absolutePath)
        val outputStream = file.outputStream()
        return OutputStreamDestination(outputStream)
    }

    @Test
    fun testMainTemplate() {
        val context = TemplateContext(sourcePath("schema/main.kte"))
        context.generateTo(output("output/main.kt"))
    }

}