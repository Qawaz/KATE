import com.wakaztahir.kate.InputSourceStream
import com.wakaztahir.kate.OutputDestinationStream
import com.wakaztahir.kate.RelativeResourceEmbeddingManager
import com.wakaztahir.kate.TemplateContext
import com.wakaztahir.kate.dsl.ModelObjectImpl
import com.wakaztahir.kate.model.model.KATEObject
import com.wakaztahir.kate.model.model.MutableKATEObject
import com.wakaztahir.kate.parser.stream.getErrorInfoAtCurrentPointer
import org.junit.Test
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class TestTemplates {

    private fun getObject(): MutableKATEObject {
        return MutableKATEObject {
            insertValue("mathTestClassName", "MathsClass")
            putObjects("arithmetic") {
                putObject {
                    insertValue("funName", "sumsTwoVars")
                    insertValue("first", 4)
                    insertValue("second", 6)
                    insertValue("symbolName", "+")
                    insertValue("returnType", "Int")
                }
                putObject {
                    insertValue("funName", "subtractTwoVars")
                    insertValue("first", 6)
                    insertValue("second", 4)
                    insertValue("symbolName", "-")
                    insertValue("returnType", "Int")
                }
                putObject {
                    insertValue("funName", "multiplyTwoVars")
                    insertValue("first", 4)
                    insertValue("second", 6)
                    insertValue("symbolName", "*")
                    insertValue("returnType", "Int")
                }
                putObject {
                    insertValue("funName", "divideTwoVars")
                    insertValue("first", 4)
                    insertValue("second", 2)
                    insertValue("symbolName", "/")
                    insertValue("returnType", "Int")
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
        val other = sourceStream.block.getValueAsString(0)
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

    private fun testTemplate(
        basePath: String,
        inputPath: String,
        outputPath: String,
        model: MutableKATEObject = MutableKATEObject { }
    ) {
        val context = getContextFor(basePath = basePath, inputPath = inputPath, model = model)
        val output = output("output/$outputPath")
        try {
            context.generateTo(output)
        } catch (e: Exception) {
            val indo = context.stream.getErrorInfoAtCurrentPointer()
            throw Throwable("${indo.first}:${indo.second}", cause = e)
        }
        output.outputStream.close()
    }

    private fun testSchemaTemplate(basePath: String, inputPath: String, outputPath: String) {
        testTemplate(basePath = "schema/$basePath", inputPath = inputPath, outputPath = "$basePath/$outputPath")
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
        testTemplate("tests", "main.kate", "main.kt", model = getObject())
    }

}