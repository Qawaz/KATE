## Input

`Model = Any Data Structure That Implements Template Model`

## Comments

`<%-- This won't go in the output --%>`

## Embedding

`@embed ./template-path.kte`

This directly copies and pastes the template into the current template , Its global scoped constants can be used inside the current template.

> Embed statements can only be used at the top of the template

## Constants

`@const variableName = "My Name"`

The value of the constant can only be

| Value                               | Supported |
|-------------------------------------|-----------|
| String                              | &check;   |
| Another Constant's Value            | &check;   |
| Value Returned from a function call | &cross;   |
| List Element                        | &cross;   |
| A model's property                  | &cross;   |

## References & Function Calls

`@const(variableName)` to get value of the variable defined earlier

`@model.property` To get a property from the model

`@model.function(value1,value2)` To call a function in the model

## Conditional Rendering

`@if(condition) @elseif(condition) @endif`

#### Conditional Operators    

`==` , `!=` , `>` , `<` , `>=` , `<=`

## List Operations

Operations that can be performed on an iterable present in the model

`@model.iterable.size` Gets the size of the list

`@model.iterable[0]` Gets the first element where zero can be any number between 0 and list.size - 1

## Looping

`@for(condition) @endfor`

This loop will run until the condition is true

`@for(@const element : @model.list) @endfor`

For each on every element of the list

`@for(@const index from 0 to @model.list.size - 1) @endfor`

## @raw

`@raw text goes directly into the template @endraw`