package object_as_struct

type NestedObject struct {
	testList []int
	testMList []float64
	testBoolean bool
	testChar rune
	testVariable string
	testDouble float64
	testLong int64
}

type TestObject struct {
	nestedObject NestedObject
	testList []int
	testMList []float64
	testBoolean bool
	testChar rune
	testVariable string
	testListList [][]int
	testDouble float64
	testLong int64
}