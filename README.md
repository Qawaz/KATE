## Features

| Value                   | Supported |
|-------------------------|-----------|
| Read From Stream / Text | &check;   |
| Write To Stream / Text  | &check;   |
| Easy , Fast & Tested    | &check;   |

## Input

`Model = Any Data Structure That Implements Template Model`

## Comments

`<%-- This won't go in the output --%>`

## Embedding

`@embed ./template-path.kte`

This directly copies and pastes the template into the current template , Its global scoped variables can be used inside
the current template.

> Embed statements can only be used at the top of the template

## Variables

`@var variableName = "My Name"`

To reassign the variable to a different value , same expression is used

The value of the variable can only be one of these

| Value                               | Supported |
|-------------------------------------|-----------|
| String ("HelloWorld")               | &check;   |
| Integer (12345)                     | &check;   |
| Float (1.0f)                        | &check;   |
| Boolean (true,false)                | &check;   |
| Another Variable's Value            | &check;   |
| [Expressions](#expressions)         | &check;   |
| Value Returned from a function call | &check;   |
| List Element                        | &check;   |
| A model's property                  | &check;   |

## References & Function Calls

`@var(variableName)` to get value of the variable defined earlier

`@model.property` To get a property from the model

`@model.function(value1,value2)` To call a function in the model

Explicit property , so compiler won't think it's a function name

`@model.@property()` in this case

## Expressions

`2 @+ 2` is an expression

`@+` means resolve it

`2 + 2` outputs `2 + 2`

`2 @+ 2` outputs `4`

You can use variables in expressions

## Conditional Rendering

`@if(condition) @elseif(condition) @else @endif`

#### Conditional Operators

`==` , `!=` , `>` , `<` , `>=` , `<=`

## List Operations

Operations that can be performed on an iterable present in the model

`@model.iterable.size` Gets the size of the list

`@model.iterable[0]` Gets the first element where zero can be any number between 0 and list.size - 1

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

Or

`@for(7) This will run seven times @endfor`

You can use a variable / expression instead of 7

To break a parent loop , You can use

`@breakfor` like this `@if(index > 5) @breakfor`

inside the for loop's block , This won't break the parent's parent loop

## @raw

`@raw this text goes directly into the template @endraw`