package variables

import GenerateCode
import GenerateExpression
import com.wakaztahir.kate.TemplateContext
import com.wakaztahir.kate.model.*
import com.wakaztahir.kate.model.model.*
import com.wakaztahir.kate.parser.*
import com.wakaztahir.kate.parser.function.KATEParsedFunction
import com.wakaztahir.kate.parser.parseExpression
import com.wakaztahir.kate.parser.stream.TextDestinationStream
import com.wakaztahir.kate.parser.variable.parseVariableDeclaration
import com.wakaztahir.kate.parser.variable.parseVariableReference
import kotlin.test.*

class VariablesTest {

    @Test
    fun testParseVariableReference() {
        val context = TemplateContext(("@var(myVar)"))
        context.stream.model.insertValue("myVar", StringValue("someValue"))
        val ref = context.stream.block.parseVariableReference(true)
        assertNotEquals(null, ref)
        assertEquals(ref!!.propertyPath[0].name, "myVar")

        assertEquals("someValue", ref.asPrimitive().value)
    }

    @Test
    fun testDeclaration(){
        val context = TemplateContext("@var x = 5")
        context.generateTo(TextDestinationStream())
        assertEquals(IntValue(5),context.stream.model.get("x"))
        context.updateStream("@var(x)")
        val destination = TextDestinationStream()
        context.generateTo(destination)
        assertEquals("5",destination.getValue())
        context.updateStream("@set_var x = 6 @var(x)")
        context.generateTo(destination)
        assertEquals(IntValue(6),context.stream.model.get("x"))
        assertEquals("56",destination.getValue())
    }

    @Test
    fun testReferencedFunction() {
        assertEquals(
            expected = "5",
            actual = GenerateCode(
                "@var i = 5 @var j = @var(i) @write(j.toString())"
            )
        )
    }

    @Test
    fun testLazyReferencedValue() {
        assertEquals(
            expected = "5",
            actual = GenerateCode("@var(i)", MutableKATEObject {
                insertValue("i", IntValue(5))
            })
        )
    }

    @Test
    fun testStringConcatenation() {
        assertEquals("helloworld", GenerateCode("@var i = \"hel\" + \"lo\" @var(i + \"world\")"))
        assertEquals("helloworld", GenerateCode("@var i = \"hel\" + \"lo\" + \"world\" @var(i)"))
    }

    @Test
    fun testFunctionValue() {
        assertEquals(
            expected = "10",
            actual = GenerateCode("@var i = @var(myFunc()) @var(i)", MutableKATEObject {
                insertValue("myFunc",
                    KATEParsedFunction("myFunc ()-> int") { model, invokedOn, explicitType, parameters ->
                        IntValue(10)
                    }
                )
            })
        )
    }

    @Test
    fun testThisObjectReference() {
        assertEquals(
            expected = "test",
            actual = GenerateCode("@var(this.var1)", MutableKATEObject { insertValue("var1", "test") })
        )
    }

    @Test
    fun testStringConversions() {
        assertEquals("123", GenerateCode("@var x = \"123\" @var(x.toInt())"))
        assertEquals("123.0", GenerateCode("@var x = \"123\" @var(x.toDouble())"))
        assertEquals("", GenerateCode("@var x = \"abc\" @var(x.toInt())"))
        assertEquals("123", GenerateCode("@var x = \"123\" @var(x.toInt().toString())"))
    }

    @Test
    fun testParseVariableGeneration() {
        val text = "@var myVar = \"someValue\" @var(myVar)"
        val context = TemplateContext(text)
        assertEquals("someValue", context.getDestinationAsString())
        assertEquals(text.length, context.stream.pointer)
    }

    @Test
    fun testListDefinition() {
        assertEquals(
            "12,55,66,77,88,99",
            GenerateCode("@var myList = @list(12,55,66,77,88,99) @var(myList)")
        )
        assertEquals(
            "12,55,66,77,88,99",
            GenerateCode("@var myList = @mutable_list(12,55,66,77,88,99) @var(myList)")
        )
        assertEquals(
            "66",
            GenerateCode("@var myList = @mutable_list(12,55,66,77,88,99) @var(myList[2])")
        )
    }

    private fun evaluate(i: String, j: String, char: Char, expect: String) {
        assertEquals(expect, GenerateCode("@var i = $i $char $j @var(i)"))
        assertEquals(expect, GenerateCode("@var i = $i @var j = @var(i) $char $j @var(j)"))
        assertEquals(expect, GenerateCode("@var j = $j @var i = $i $char @var(j) @var(i)"))
    }

    @Test
    fun testExpressions() {
        evaluate("0", "2", '+', "2")
        evaluate("2", "0", '+', "2")
        evaluate("10", "5", '-', "5")
        evaluate("5", "10", '-', "-5")
        evaluate("5", "2", '*', "10")
        evaluate("2", "5", '*', "10")
        evaluate("10", "2", '/', "5")
        evaluate("2", "2", '%', "0")
    }

    @Test
    fun testBodmasRule() {
        assertEquals("3", GenerateExpression("1 + 2"))
        assertEquals("1 + 2", GenerateCode("1 + 2"))
        assertEquals("6", GenerateExpression("1 + 2 + 3"))
        assertEquals("4", GenerateExpression("2 + 4 / 2"))
        assertEquals("4", GenerateExpression("4 / 2 + 2"))
        // Addition and Subtraction tests
        assertEquals("3", GenerateExpression("1 + 2"))
        assertEquals("-1", GenerateExpression("1 - 2"))
        assertEquals("0", GenerateExpression("1 + 2 - 3"))
        assertEquals("5", GenerateExpression("1 - 2 + 6"))
        // Multiplication and Division tests
        assertEquals("4", GenerateExpression("2 * 2"))
        assertEquals("3", GenerateExpression("6 / 2"))
        assertEquals("6", GenerateExpression("2 * 3 / 1"))
        assertEquals("8", GenerateExpression("8 / 2 * 2"))
        // BODMAS rule tests
        assertEquals("6", GenerateExpression("2 + 2 * 2"))
        assertEquals("8", GenerateExpression("2 * 2 + 4"))
        assertEquals("10", GenerateExpression("2 + 2 * 5 - 2"))
        assertEquals("-10", GenerateExpression("2 - 2 * 5 - 2"))
        assertEquals("-5", GenerateExpression("2 - 2 / 2 - 3 * 2"))
    }

    @Test
    fun testReassignment() {
        val context = TemplateContext("@var i = 0 @set_var i = 2 @var(i)")
        assertEquals("2", context.getDestinationAsString())
        val context2 = TemplateContext("@var i = 10 @set_var i = @var(i) + 1 @var(i)")
        assertEquals("11", context2.getDestinationAsString())
    }

    @Test
    fun testParseStringValue() {
        val context = TemplateContext(("\"someValue\""))
        val value = context.stream.parseStringValue()!!.value
        assertEquals("someValue", value)
    }

    @Test
    fun testAtLessAndDirectRefsDeclaration() {
        assertEquals(
            expected = "he",
            actual = GenerateCode("@var i = \"h\" + \"e\" @var(i)")
        )
        assertEquals(
            expected = "15",
            actual = GenerateCode("@var i = 10 + 5 @var(i)")
        )
        assertEquals(
            expected = "5",
            actual = GenerateCode("@var i = 15 / 3 @var(i)")
        )
        assertEquals(
            expected = "15",
            actual = GenerateCode("@var i = 3 * 5 @var(i)")
        )
        assertEquals(
            expected = "he",
            actual = GenerateCode("@var j = \"e\" @var i = \"h\" + @var(j) @var(i)")
        )
        assertEquals(
            expected = "15",
            actual = GenerateCode("@var j = 5 @var i = 10 + @var(j) @var(i)")
        )
        assertEquals(
            expected = "5",
            actual = GenerateCode("@var j = 3 @var i = 15 / @var(j) @var(i)")
        )
        assertEquals(
            expected = "15",
            actual = GenerateCode("@var j = 5 @var i = 3 * @var(j) @var(i)")
        )
        assertEquals(
            expected = "he",
            actual = GenerateCode("@var j = \"e\" @var i = \"h\" + j @var(i)")
        )
        assertEquals(
            expected = "15",
            actual = GenerateCode("@var j = 5 @var i = 10 + j @var(i)")
        )
        assertEquals(
            expected = "5",
            actual = GenerateCode("@var j = 3 @var i = 15 / j @var(i)")
        )
        assertEquals(
            expected = "15",
            actual = GenerateCode("@var j = 5 @var i = 3 * j @var(i)")
        )
    }

    @Test
    fun testParseDynamicProperty() {
        val context = TemplateContext(("@var(myVar)"))
        context.stream.model.insertValue("myVar", StringValue("someValue"))
        val property = context.stream.block.parseExpression(
            parseDirectRefs = true
        )
        assertEquals(property!!.asPrimitive().value, "someValue")
    }

    @Test
    fun testDeclarationAndReference() {
        val decContext = TemplateContext(("@var myVar = \"someValue\""))
        val dec = decContext.stream.block.parseVariableDeclaration()
        assertEquals("someValue", dec!!.variableValue.asPrimitive().value)
        decContext.updateStream("@var myVar2 = @var(myVar)")
        dec.storeValue()
        val refDec = decContext.stream.block.parseVariableDeclaration()
        assertEquals(
            dec.variableValue.asPrimitive().value,
            refDec!!.variableValue.asPrimitive().value
        )
    }

}