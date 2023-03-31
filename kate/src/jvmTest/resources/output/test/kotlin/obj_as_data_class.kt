package test_object_as_data_class

data class NestedObject (
    val testList : List<Int>,
    val testMList : List<Double>,
    val testBoolean : Boolean,
    val testChar : Char,
    val testVariable : String,
    val testDouble : Double,
    val testLong : Long,
)

data class TestObject (
    val nestedObject : NestedObject,
    val testList : List<Int>,
    val testMList : List<Double>,
    val testBoolean : Boolean,
    val testChar : Char,
    val testVariable : String,
    val testListList : List<List<Int>>,
    val testDouble : Double,
    val testLong : Long,
)