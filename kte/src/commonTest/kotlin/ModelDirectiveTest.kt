import com.wakaztahir.kte.GenerateCode
import com.wakaztahir.kte.TemplateContext
import com.wakaztahir.kte.model.*
import com.wakaztahir.kte.model.model.*
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
    fun testObjectGeneration() {
        val context = TemplateContext("@model.MyObject")
        context.stream.model.apply {
            this.putObject("MyObject") {
                this.putValue("myInt", 15)
                this.putValue("myDouble", 16.000)
                this.putValue("myStr", "something is here")
                this.putValue(
                    key = "myList", value = KTEListImpl<IntValue>(
                        objectName = "myList", collection = listOf(
                            IntValue(10),
                            IntValue(20),
                            IntValue(30),
                            IntValue(40),
                        )
                    )
                )
                this.putObject(key = "MyNestedObject") {}
            }
        }
        assertEquals(
            """data class MyObject(
            |	myDouble : Double = 16.0,
            |	myList : List<Int> = listOf(10, 20, 30, 40),
            |	MyNestedObject : Any = MyNestedObject(),
            |	myInt : Int = 15,
            |	myStr : String = "something is here"
            |)
            |
            |data class MyNestedObject(
            |
            |)""".trimMargin("|"), context.getDestinationAsString()
        )
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
        val context = TemplateContext("@model.arithmetic.funName@var(arithmetic).funName", MutableKTEObject {
            putObject("arithmetic") {
                putValue("funName", "seriouslyHere")
            }
        })
        assertEquals("seriouslyHereseriouslyHere", context.getDestinationAsString())
    }

    @Test
    fun testFunction() {
        var invocations = 0
        val myFunc = object : KTEFunction() {
            override fun invoke(model: KTEObject, parameters: List<ReferencedValue>): KTEValue {
                invocations++
                return StringValue("funVal")
            }

            override fun toString(): String = "funName()"
        }
        val context = TemplateContext("@model.funName()@model.@@propName()@model.@funName()", MutableKTEObject {
            putValue("funName", myFunc)
            putValue("propName", "propVal")
        })
        assertEquals("funValpropVal()", context.getDestinationAsString())
        assertEquals(2, invocations)
    }

    @Test
    fun testParseModelDirectiveCodeGen() {
        val model = MutableKTEObject {
            putValue("property1", true)
            putObject("property2") {
                putValue("property3", "123")
            }
            putValue("callSum", object : KTEFunction() {
                override fun invoke(model: KTEObject, parameters: List<ReferencedValue>): KTEValue {
                    return IntValue(parameters.map { it.asPrimitive(model) }.sumOf { it.value as Int })
                }

                override fun toString(): String = "callSum(integers) : Int"
            })
        }
        assertEquals("true1233", GenerateCode("@model.property1@model.property2.property3@model.callSum(1,2)", model))
        assertEquals("3", GenerateCode("@var sum = @model.callSum(1,2) @var(sum)", model))
    }

}