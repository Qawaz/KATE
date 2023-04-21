import com.wakaztahir.kate.InputSourceStream
import com.wakaztahir.kate.OutputDestinationStream
import com.wakaztahir.kate.RelativeResourceEmbeddingManager
import com.wakaztahir.kate.TemplateContext
import com.wakaztahir.kate.dsl.ModelObjectImpl
import com.wakaztahir.kate.model.model.KATEObject
import com.wakaztahir.kate.model.model.MutableKATEObject
import org.junit.Test
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class TestTemplates {

    private fun getObject(): MutableKATEObject {
        return MutableKATEObject {
            setValue("mathTestClassName", "MathsClass")
            putObjects("arithmetic") {
                putObject {
                    setValue("funName", "sumsTwoVars")
                    setValue("first", 4)
                    setValue("second", 6)
                    setValue("symbolName", "+")
                    setValue("returnType", "Int")
                }
                putObject {
                    setValue("funName", "subtractTwoVars")
                    setValue("first", 6)
                    setValue("second", 4)
                    setValue("symbolName", "-")
                    setValue("returnType", "Int")
                }
                putObject {
                    setValue("funName", "multiplyTwoVars")
                    setValue("first", 4)
                    setValue("second", 6)
                    setValue("symbolName", "*")
                    setValue("returnType", "Int")
                }
                putObject {
                    setValue("funName", "divideTwoVars")
                    setValue("first", 4)
                    setValue("second", 2)
                    setValue("symbolName", "/")
                    setValue("returnType", "Int")
                }
            }
        }
    }

    private fun output(path: String): OutputDestinationStream {
        val file = File("src/jvmTest/resources/$path")
        println(file.absolutePath)
        val outputStream = file.outputStream()
        return OutputDestinationStream(outputStream)
    }

    @Test
    fun testInputSourceStream() {
        val input = object {}.javaClass.getResource("tests/main.kate")!!.openStream()
        val reader = input.bufferedReader()
        val text = reader.readText()
        reader.close()
        val sourceStream = InputSourceStream(
            inputStream = object {}.javaClass.getResource("tests/main.kate")!!.openStream(),
            model = getObject()
        )
        assertFalse(sourceStream.hasEnded)
        val other = sourceStream.getValueAsString(0)
        assertEquals(text, other)
    }

    private fun getContextFor(
        basePath: String,
        inputPath: String,
        model: MutableKATEObject = MutableKATEObject { }
    ): TemplateContext {
        val embedding = RelativeResourceEmbeddingManager(basePath, object {}.javaClass)
        return TemplateContext(
            stream = InputSourceStream(
                inputStream = embedding.getStream(inputPath),
                model = model,
                embeddingManager = embedding,
            )
        )
    }

    private fun testTemplateAsText(
        basePath: String,
        inputPath: String,
        model: MutableKATEObject = MutableKATEObject { }
    ): String {
        return getContextFor(basePath = basePath, inputPath, model = model).getDestinationAsString()
    }

    private fun testSchemaTemplate(basePath: String, inputPath: String, outputPath: String) {
        val context = getContextFor(basePath = "schema/$basePath", inputPath = inputPath)
        val output = output("output/$basePath/$outputPath")
        context.generateTo(output)
        output.outputStream.close()
    }

    @Test
    fun testEmbedOnce() {
        assertEquals(
            expected = "Works",
            actual = testTemplateAsText("tests/embedding", "test.kate")
        )
    }

    @Test
    fun testRawTemplates() {
        val basePath = "test/raw"
        testSchemaTemplate(basePath, "raw_obj.kate", "raw_obj.kate")
    }

    @Test
    fun testJsonObjectTemplate() {
        val basePath = "test/json"
        testSchemaTemplate(basePath, "object_as_json.kate", "object_as_json.json")
    }

    @Test
    fun testObjectAsGoStruct() {
        val basePath = "test/golang"
        testSchemaTemplate(basePath, "object_as_golang.kate", "object_as_golang.go")
    }

    @Test
    fun testKotlinTemplates() {
        val basePath = "test/kotlin"
        testSchemaTemplate(basePath, "obj_as_data_class.kate", "obj_as_data_class.kt")
        testSchemaTemplate(basePath, "obj_as_interface.kate", "obj_as_interface.kt")
        testSchemaTemplate(basePath, "obj_override_interface_data_class.kate", "obj_override_interface_data_class.kt")
    }

    @Test
    fun testGenerateTemplates() {
        testTemplateAsText("tests","main.kate",model = getObject())
    }

}