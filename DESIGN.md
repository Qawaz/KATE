# KATE

The design document of __KATE__ , Kotlin Adaptive Template Engine , The document defines syntax and language behaviour

## Comments

`<%-- This won't go in the output --%>`

## Embedding

`@embed ./template-path.kte`

This directly copies and pastes the template into the current template , Its global scoped variables can be used inside
the current template.

`@embed_once ./template-path.kte`

embed template once , If it has been embedded in the template indirectly (embedding a template that also embeds
this template), It won't be embedded again

## Variables

### Declaration

`@var variableName = "My Name"`

To explicitly give type to the variable use

`@var variableName : string = "My Name"`

You can give `string`,`char`,`boolean`,`int`,`double`,`long`,`list<ItemType>`,`mutable_list<ItemType>`,`object<ItemType>`

### Assignment

`@set_var variableName = "otherValue"`

If the variable doesn't exist , It will cause an error

__KATE__ also supports assignment operators `+`,`-`,`*`,`/`,`%` so you can also `@set_var i *= j`

In Partial Raw , You can skip `@set_var`

### Variable Value

The value of the variable can only be one of these

| Value                                                      | Supported |
|------------------------------------------------------------|-----------|
| [Primitives (Char,String,Int,Double,Boolean)](#primitives) | &check;   |
| Reference to another variable                              | &check;   |
| [Expressions](#expressions)                                | &check;   |
| Value Returned from a function call                        | &check;   |
| [List or Its element](#lists)                              | &check;   |

## References & Function Calls

`@var(variableName)` to get value of the variable defined earlier

To assign to another variable use `@var i = j`

To access the object of current scope , You can use `@var(this)`

To invoke a function use `@var(funcName())`

Invoking a function without outputting returned value

`@partial_raw funcName() @end_partial_raw`

Any reference encapsulated within `@var()` will always output its value

### getType()

Every variable has a `getType` function available which returns type in string format

| Variable               | Returned Type  |
|------------------------|----------------|
| boolean.getType()      | "boolean"      |
| char.getType()         | "char"         |
| string.getType()       | "string"       |   
| double.getType()       | "double"       |
| int.getType()          | "int"          |
| list.getType()         | "list"         |
| mutable_list.getType() | "mutable_list" |
| object.getType()       | "object<{}>"   |
| unit.getType()         | "unit"         |

Every variable also has a `toString` function to convert to a string

## Expressions

To output an expression , The whole expression can be written inside `@var`

for example

`@var(1 + 2)` outputs `3`

`@var i = 15 @var(i + 5)` outputs `20`

> Brackets not supported at the moment

## Conditional Output

`@if(condition) @elseif(condition) @else @endif`

#### Conditional Operators

`==` , `!=` , `>` , `<` , `>=` , `<=`

## For Loop

There's only one type of loop and that's for loop

This loop will run until the condition is true

`@for(condition) @endfor`

For each on every element of the list

`@for(@var element : @model.list) @endfor`

To get the index of every element in the list

`@for(@var element,index : @model.list) @endfor`

To loop using a number

`@for(@var i=0;i<5;i++) @endfor`

To break a parent loop , You can use

`@breakfor` like this `@if(index > 5) @breakfor`

inside the for loop's block , This won't break the parent's parent loop

## Raw Block

If you need to just output raw text , You should use the raw block

`@raw this text goes directly into the template @endraw`

## Partial Raw

Partial Raw , It only outputs the code that is generated via directives

```
@partial_raw
    <%--raw text is not allowed here--%>
@end_partial_raw
```

- By default , All text is outputted and Directives output after being parsed
- By using raw , You output everything without any parsing
- By using partial_raw , You output explicitly by using nested `@default_no_raw` or `@raw` block

Inside the `@partial_raw` block , To go back to default behaviour , You can use

```
@default_no_raw
    raw text is allowed here and so are the directives
@end_default_no_raw
```

`@partial_raw` and `@default_no_raw` inherit scope of parent
so any variables / lists / objects created inside these blocks will be
accessible outside of these blocks

## Lists

List is basically an array like data structure , In KTE , Lists can be created to store objects of a single type.

To create a list

`@var myList = @mutable_list(1,2,3)`

`@var myList2 = @list(1,2,3)`

All elements in the list must be of a single type

### List Functions

List supports the following properties and functions , You can use
`@var(myList.size())` to get the size of the list

#### Immutable List Functions

| Value                                 | Description                                       |
|---------------------------------------|---------------------------------------------------|
| size() : int                          | Returns the size of the list                      |
| get(index : int) : element            | Returns the element at index                      |
| contains(e : element) : boolean       | Returns true if element exists                    |
| indexOf(e : element) : int            | Returns index of element (-1 if not found)        |
| toString(separator : string) : string | converts list to string with separator (optional) |

#### Mutable List Functions

Mutable List also has functions of Immutable List

| Value                                 | Description              |
|---------------------------------------|--------------------------|
| add(e : element) : boolean            | Add element at last      |
| addAt(index : int,e : element) : unit | Add element at index     |
| remove(e : element) : boolean         | Remove element from list |
| removeAt(index : int) : element       | Remove element at index  |

## Objects

An object can be created to hold variables of different types , An object block only expects variable / function
declarations

```
@define_object(MyObject)
    @var i = 5
    @var myList = @mutable_list(1,2,3)
@end_define_object
```

And then objects can be referenced just like any other variable `@var(MyObject)`

Here's a table of functions that exist on objects

| Value                                        | Description                                         |
|----------------------------------------------|-----------------------------------------------------|
| getKeys() : List<string>                     | Get keys of the object                              |
| getValues() : List<KTEValue>                 | Get values of the object                            |
| contains(name : string) : boolean            | Returns true if contains key                        |
| containsInAncestors(name : string) : boolean | Returns true if contains key in it or its ancestors |
| delete(name : string) : unit                 | Removes the key if exists                           |
| rename(key : string,with : string) : unit    | Renames a child key to 'with' key                   |

## Primitives

### Strings

A string can be defined like this

`@var str = "my string"`

Here's a table of functions available

| Function                            | Description                                                       |
|-------------------------------------|-------------------------------------------------------------------|
| str[0]                              | Indexing , Equivalent to .get(0) returns first char               |
| size() : int                        | Returns the size as an int                                        |
| toInt() : int                       | Tries to convert to int , or returns Unit                         |
| toDouble() : double                 | Tries to convert to double , or returns Unit                      |
| substring(0,5) : string             | Returns a substring from 0 (inclusive) to 5 (exclusive)           |
| uppercase() : string                | Convert the whole string to uppercase                             |
| lowercase() : string                | Convert the whole string to lowercase                             |
| capitalize() : string               | Capitalize the first character                                    |
| decapitalize() : string             | Decapitalize the first character                                  |
| replace("find","replace") : string  | Replace the string's find with replace value                      |
| contains("") : string               | Returns true if contains the string                               |
| indexOf(str : string) : Int         | index of string within string or -1                               |
| split(str : string) : List<string>  | split at str and convert to a list                                |
| startsWith(str : string) : boolean  | returns true if the string starts with given string else false    |
| endsWith(str : string) : boolean    | returns true if the string ends with given string else false      |
| removePrefix(str : string) : string | returns string after removing prefixed str , or original if fails |
| removeSuffix(str : string) : string | returns string after removing prefixed str , or original if fails |

### Integers

An integer can be defined like this `@var i = 0`

| Function            | Description                     |
|---------------------|---------------------------------|
| toString() : string | converts this integer to string |
| toDouble() : double | converts this integer to double |

Similar functions are available on a `Double`

## Placeholders

Placeholders are great if you would like to Output some content multiple times
at different places in the template

### Definition

To define a placeholder , You use `@define_placeholder`

`@define_placeholder(PlaceholderName,DefinitionName)`

You can also do `@define_placeholder_once` to not cause an error if it already exists

```
@define_placeholder(WelcomeText,GreetingText,PersonName)
    Hello World ! How are you @var(PersonName)?
@end_define_placeholder
```

`@define_placeholder` takes can take three parameters

| Parameter       | Optional | Default         |
|-----------------|----------|-----------------|
| PlaceholderName | False    |                 |
| DefinitionName  | True     | PlaceholderName |
| Parameter Name  | True     | `__param__`     |

If you define a placeholder with a single parameter , The same name will be assigned to both,
the placeholder and its definition and `__param__` will be used as parameter name

To skip passing Definition Name while passing parameter name , You can put two commas like
this `@define_placeholder(WelcomeText,,PersonName)`

### Invocation

To invoke / call a placeholder You use `@placeholder` with Placeholder name as the parameter

```
@placeholder(WelcomeText)
```

`@placeholder` invocation expects three parameters

| Parameter       | Optional | Default         |
|-----------------|----------|-----------------|
| PlaceholderName | False    |                 |
| DefinitionName  | True     | PlaceholderName |
| Parameter Value | True     | Nothing         |

To skip passing Definition Name while passing parameter value , You can put two commas like
this `@placeholder(WelcomeText,,"Guy Name")`

Placeholders inherit the scope of invocation , That's why this code is possible

```
@define_placeholder(Variable)
    @var(i)
@end_define_placeholder

@for(@var i=0;i<5;i++)
    @placeholder(Variable)
@endfor

<%-- 01234 --%>
```

You can change the name of parameter by passing the third parameter , You can skip passing the definition name so
placeholder name would be used for it

```
@define_placeholder(Variable,,scope)
    @var(scope.i)
@end_define_placeholder
```

Passing a custom object to placeholder

```
@define_object(MyObject)
    @var myVar = 5
@end_define_object

@placeholder(WelcomeText,,MyObject)
```

Now welcome text can make a reference to `myVar` and it will be able to access it using `@var(__param__.myVar)`

Otherwise `MyObject` is still accessible inside the placeholder because scope is being inherited

You can check if the parameter has been passed to placeholder using `@var(this.contains("__param__"))`

### Redefinition

You can redefine and provide your own implementation for the next code that will be generated

```
@define_placeholder(WelcomeText,NewsText)
    We've detected an earthquake
@end_define_placeholder
```

Now when you invoke / call the placeholder , `NewsText` will be used instead of `GreetingText`

If you'd like to go back to previous implementation , You need the definition name that was previously used to define it

```
@use_placeholder(WelcomeText,GreetingText)
```

If the definition name of the placeholder is same as Placeholder Name , You can skip passing
the definition name when calling `@use_placeholder`

Now when you invoke / call the placeholder , `GreetingText` will be used again , instead of `NewsText`

## Runtime Write

To write a value to output, Any value can be written

| Directive         | Description                    |
|-------------------|--------------------------------|
| `@write('x')`     | Prints the character to output |
| `@write("hello")` | Prints the string to output    |

## Function Definition

To define a function

```
@function myFunc(param1)
    @return @var(param1)
@end_function
```

To invoke the function `@var(myFunc("myOwnStr"))` can be used

The code above returns the first parameter passed to the function using indexing operator which translates to `get(0)`

Nothing is outputted directly like `@partial_raw` , explicit output is required in functions