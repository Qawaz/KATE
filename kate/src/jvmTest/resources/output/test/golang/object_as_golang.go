package object_as_struct

type NestedObject struct {
	testList []int
	testMList []float32
	testBoolean bool
	testChar rune
	testVariable string
	testDouble float32
	testLong int64
}

type TestObject struct {
	nestedObject NestedObject
	testList []int
	testMList []float32
	testBoolean bool
	testChar rune
	testVariable string
	testListList [][]int
	testDouble float32
	testLong int64
}