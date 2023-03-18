import com.wakaztahir.kte.InputStreamSource
import com.wakaztahir.kte.TemplateContext
import com.wakaztahir.kte.parser.parse
import org.junit.Test

class TestTemplates {

    private fun sourcePath(path: String): InputStreamSource {
        return InputStreamSource(object {}.javaClass.getResource(path)!!.openStream())
    }

    @Test
    fun testMainTemplate() {
        val context = TemplateContext(sourcePath("schema/main.kte"))
//        context.parse()
    }

}