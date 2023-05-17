package test_object_as_interface

interface NestedObject {
	val testList : List<Int>
	val testMList : List<Double>
	val testBoolean : Boolean
	val testChar : Char
	val testVariable : String
	val testDouble : Double
	val testLong : Long
}

interface TestObject {
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