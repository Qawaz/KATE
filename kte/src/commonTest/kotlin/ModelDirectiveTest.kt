import com.wakaztahir.kte.TemplateContext
import com.wakaztahir.kte.dsl.ModelValue
import com.wakaztahir.kte.model.KTEFunction
import com.wakaztahir.kte.model.model.TemplateModel
import com.wakaztahir.kte.model.model.KTEObject
import com.wakaztahir.kte.model.ModelDirective
import com.wakaztahir.kte.model.ModelReference
import com.wakaztahir.kte.model.ReferencedValue
import com.wakaztahir.kte.parser.parseExpression
import com.wakaztahir.kte.parser.parseModelDirective
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ModelDirectiveTest {

    private inline fun TemplateContext.testDirective(block: (ModelDirective) -> Unit) {
        val previous = stream.pointer
        block(stream.parseExpression() as ModelDirective)
        stream.decrementPointer(stream.pointer - previous)
        block(stream.parseModelDirective()!!)
    }

    @Test
    fun testParseModelDirective() {
        val context = TemplateContext("@model.firstProp.secondProp.thirdCall().fourthProp.fifthProp(true,false)")
        context.testDirective { directive ->
            assertEquals("firstProp", directive.propertyPath[0].name)
            assertEquals("secondProp", directive.propertyPath[1].name)
            assertEquals("thirdCall", directive.propertyPath[2].name)
            assertEquals("fourthProp", directive.propertyPath[3].name)
            val call = directive.propertyPath[4] as ModelReference.FunctionCall
            assertEquals("fifthProp", call.name)
            assertEquals(true, call.parametersList[0].asPrimitive(context.stream.model).value)
            assertEquals(false, call.parametersList[1].asPrimitive(context.stream.model).value)
        }
    }

    @Test
    fun testModelFunctionCallWithParameters() {
        val context = TemplateContext(text = "@model.callSum(1,2)")
        val directive = context.stream.parseModelDirective()!!
        assertTrue(directive.propertyPath[0] is ModelReference.FunctionCall)
    }

    @Test
    fun testForLoopGeneration6() {
        val context = TemplateContext("@model.arithmetic.funName", TemplateModel {
            putObject("arithmetic") {
                putValue("funName", "seriouslyHere")
            }
        })
        assertEquals("seriouslyHere", context.getDestinationAsString())
    }

    @Test
    fun testParseModelDirectiveCodeGen() {
        val context = TemplateContext(
            text = "@model.property1@model.property2.property3@model.callSum(1,2)",
            model = TemplateModel {
                putValue("property1", true)
                putObject("property2") {
                    putValue("property3", "123")
                }
                putValue("callSum", object : KTEFunction {
                    override fun invoke(model: KTEObject, parameters: List<ReferencedValue>): ModelValue {
                        return ModelValue(parameters.map { it.asPrimitive(model) }.sumOf { it.value as Int })
                    }

                    override fun toString(): String = "callSum(integers) : Int"
                })
            }
        )
        assertEquals("true1233", context.getDestinationAsStringWithReset())
    }

}