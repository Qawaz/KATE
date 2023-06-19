import com.wakaztahir.kate.InputParserSourceStream
import com.wakaztahir.kate.OutputDestinationStream
import com.wakaztahir.kate.RelativeResourceEmbeddingManager
import com.wakaztahir.kate.TemplateContext
import com.wakaztahir.kate.model.model.MutableKATEObject
import com.wakaztahir.kate.parser.stream.printErrorLineNumberAndCharacterIndex
import org.junit.Test
import java.io.File
import java.io.InputStream
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
        val outputStream = file.outputStream()
        return OutputDestinationStream(outputStream)
    }

    @Test
    fun testInputSourceStream() {
        val input = object {}.javaClass.getResource("tests/main.kate")!!.openStream()
        val reader = input.bufferedReader()
        val text = reader.readText()
        reader.close()
        val sourceStream = InputParserSourceStream(
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
            stream = InputParserSourceStream(
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

    private fun compareInputStreams(path: String, first: InputStream, second: InputStream) {
        var lines = 1
        var charAt = 0
        var firstChar = first.read()
        var secondChar = second.read()
        while (firstChar != -1 && secondChar != -1) {
            if (firstChar != secondChar) {
                throw IllegalStateException("${firstChar.toChar()} != ${secondChar.toChar()} in path $path($lines:$charAt)")
            } else {
                charAt++
                if (firstChar.toChar() == '\n') {
                    lines++
                    charAt = 0
                }
            }
            firstChar = first.read()
            secondChar = second.read()
        }
        if (firstChar == -1 && secondChar == -1) {
            return
        } else {
            throw IllegalStateException("${firstChar.toChar()} != ${secondChar.toChar()} in path $path($lines:$charAt)")
        }
    }

    private fun testTemplate(
        basePath: String,
        inputPath: String,
        outputPath: String,
        expectedPath: String = outputPath,
        model: MutableKATEObject = MutableKATEObject { }
    ) {
        val context = getContextFor(basePath = basePath, inputPath = inputPath, model = model)
        val outputFile = File("src/jvmTest/resources/output/$outputPath")
        val output = OutputDestinationStream(outputFile.outputStream())
        try {
            context.generateTo(output)
        } catch (e: Exception) {
            context.stream.printErrorLineNumberAndCharacterIndex()
            throw e
        }
        output.outputStream.close()
        val generatedStream = outputFile.inputStream()
        val expectedFile = File("src/jvmTest/resources/expected/$expectedPath")
        val expectedStream = expectedFile.inputStream()
        compareInputStreams("src/jvmTest/resources/output/$outputPath",generatedStream,expectedStream)
        generatedStream.close()
        expectedStream.close()
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