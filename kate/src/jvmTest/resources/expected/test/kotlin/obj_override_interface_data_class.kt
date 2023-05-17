package test_object_as_overridden_interface

interface INestedObject {
	val testList : List<Int>
	val testMList : List<Double>
	val testBoolean : Boolean
	val testChar : Char
	val testVariable : String
	val testDouble : Double
	val testLong : Long
}

interface ITestObject {
	val nestedObject : NestedObject
	val testList : List<Int>
	val testMList : List<Double>
	val testBoolean : Boolean
	val testChar : Char
	val testVariable : String
	val testListList : List<List<Int>>
	val testDouble : Double
	val testLong : Long
}

data class NestedObject (
    override val testList : List<Int>,
    override val testMList : List<Double>,
    override val testBoolean : Boolean,
    override val testChar : Char,
    override val testVariable : String,
    override val testDouble : Double,
    override val testLong : Long,
) : INestedObject

data class TestObject (
    override val nestedObject : NestedObject,
    override val testList : List<Int>,
    override val testMList : List<Double>,
    override val testBoolean : Boolean,
    override val testChar : Char,
    override val testVariable : String,
    override val testListList : List<List<Int>>,
    override val testDouble : Double,
    override val testLong : Long,
) : ITestObject