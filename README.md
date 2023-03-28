## Features

| Value                   | Supported |
|-------------------------|-----------|
| Read From Stream / Text | &check;   |
| Write To Stream / Text  | &check;   |
| Easy , Fast & Tested    | &check;   |

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

`@var variableName = "My Name"`

To reassign the variable to a different value , same expression is used

The value of the variable can only be one of these

| Value                                                      | Supported |
|------------------------------------------------------------|-----------|
| [Primitives (Char,String,Int,Double,Boolean)](#primitives) | &check;   |
| Reference to another variable                              | &check;   |
| [Expressions](#expressions)                                | &check;   |
| Value Returned from a function call                        | &check;   |
| [List or Its element](#lists)                              | &check;   |

### Variable Directives

#### Exists Directive

`@exists_var(myVar)` checks if `myVar` exists , returns true if it does

#### Delete Directive

`@delete_var(myVar)` deletes any value , This can remove objects / lists / primitives and functions from the object

## References & Function Calls

`@var(variableName)` to get value of the variable defined earlier

To assign to another variable

`@var i = @var(j)`

To access the object of current scope , You can do

`@var(this)`

To invoke a function

`@var(funcName())`

Invoking a function without outputting returned value

`@var(@funcName())`

## Expressions

`2 @+ 2` is an expression

`@+` means resolve it

`2 + 2` outputs `2 + 2`

`2 @+ 2` outputs `4`

You can use variables in expressions

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

| Value                           | Description                                |
|---------------------------------|--------------------------------------------|
| size() : Int                    | Returns the size of the list               |
| get(index : Int) : Element      | Returns the element at index               |
| contains(e : Element) : Boolean | Returns true if element exists             |
| indexOf(e : Element) : Int      | Returns index of element (-1 if not found) |

#### Mutable List Functions

Mutable List also has functions of Immutable List

| Value                                 | Description              |
|---------------------------------------|--------------------------|
| add(e : Element) : Boolean            | Add element at last      |
| addAt(index : Int,e : Element) : Unit | Add element at index     |
| remove(e : Element) : Boolean         | Remove element from list |
| removeAt(index : Int) : Element       | Remove element at index  |

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

## Primitives

### Strings

A string can be defined like this

`@var str = "my string"`

Here's a table of functions available

| Function                              | Description                                             |
|---------------------------------------|---------------------------------------------------------|
| `@var(str.size())`                    | Returns the size as an int                              |
| `@var(str[0])`                        | Equivalent to .get(0) returns first char                |
| `@var(str.toInt())`                   | Tries to convert to int , or returns Unit               |
| `@var(str.toDouble())`                | Tries to convert to double , or returns Unit            |
| `@var(str.substring(0,5))`            | Returns a substring from 0 (inclusive) to 5 (exclusive) |
| `@var(str.uppercase())`               | Convert the whole string to uppercase                   |
| `@var(str.lowercase())`               | Convert the whole string to lowercase                   |
| `@var(str.capitalize())`              | Capitalize the first character                          |
| `@var(str.decapitalize())`            | Decapitalize the first character                        |
| `@var(str.replace("find","replace"))` | Replace the string's find with replace value            |
| `@var(str.contains(""))`              | Returns true if contains the string                     |

### Integers

An integer can be defined like this `@var i = 0`

| Function             | Description                     |
|----------------------|---------------------------------|
| `@var(i.toString())` | converts this integer to string |
| `@var(i.toDouble())` | converts this integer to double |

Similar functions are available on a `Double`

## Placeholders

Placeholders are great if you would like to Output some content multiple times
at different places in the template

### Definition

To define a placeholder , You use `@define_placeholder`

`@define_placeholder(PlaceholderName,DefinitionName)`

```
@define_placeholder(WelcomeText,GreetingText)
    Hello World ! How are you ?
@end_define_placeholder
```

Placeholders take two parameters , First the name of the placeholder
and second the name of the definition of placeholder

If you define a placeholder with a single parameter , The same name will be assigned to both,
the placeholder and its definition

### Invocation

To invoke / call a placeholder You use `@placeholder` with Placeholder name as the parameter

```
@placeholder(WelcomeText)
```

Placeholders inherit scope of invocation directive , Because of this, this code is possible

```
@define_placeholder(Varible)
    @var(i)
@end_define_placeholder

@for(@var i=0;i<5;i++)
    @placeholder(Variable)
@endfor
```

This means that placeholder block inherits the scope of invocation directive , so any
variables / lists / objects that are in the scope of invocation will be
accessible inside the placeholder definition

You can also provide an object whose variables will be in the scope of placeholder

```
@define_object(MyObject)
    @var myVar = 5
@end_define_object

@placeholder(WelcomeText,@var(MyObject))
```

Now welcome text can make a reference to `myVar` and it will be able to access it

If placeholder creates any variables , They'll be present in `MyObject` after the invocation

Values provided to placeholder can be accessed using `@var(this)` , Values other than objects can
be accessed this way inside the placeholder

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

## Runtime Directives

Runtime directives call functions in the runtime , Currently these directives are available

| Directive                        | Description                    |
|----------------------------------|--------------------------------|
| `@runtime.print_char('x')`       | Prints the character to output |
| `@runtime.print_string("hello")` | Prints the string to output    |

## Function Definition

To define a function

```
@function(myFunc)
    @return 1
@end_function
```

Parameters are available in a function definition using `@var(this)` which is a list of parameters

```
@function(firstParam)
    @return @var(this[0])
@end_function
```

To invoke the function `@var(myFunc)` , `@var(firstParam)` can be used , prefixed with `@` if you don't want to output
returned value

The code above returns the first parameter passed to the function using indexing operator which translates to `get(0)`

#### Keep in mind

When defining a function following rules apply

- Nothing is outputted directly like `@partial_raw` , explicit output is required
- Function must return something , `@return @Unit` can be used if there is nothing to return