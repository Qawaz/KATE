# KATE

__KATE__ stands for Kotlin Adaptive Template Engine , KATE is very adaptive and
you'd find it true to its name.

## Usage

Create a source stream , You can use `TextSourceStream` or `InputSourceStream` if you are on jvm

```
val context = TemplateContext(TextSourceStream("@var i = 5 @var(i)"))
```

If you'd like to get output as text , You can do

```
context.getDestinationAsString(): String
```

If you'd like to write output to output stream on jvm

```
val output = file.outputStream()
val stream = OutputDestinationStream(output)
context.stream.generateTo(stream)
output.close()
```

## How KATE works

For the full design please see [KATE Design](./DESIGN.md)

### Modes

__KATE__ has three modes

1 - Default No Raw (Language is parsed and Text is outputted)

2 - Partial Raw (Only language is parsed and explicit output is required)

3 - Raw (Text is outputted as it is)

### Variable Reference To Placeholder Invocation

Placeholders are called under the hood that output everything you see , Automatic conversion of variable reference to
placeholder invocation takes place in Default No Raw mode only

You can think of placeholders as functions for now even though __KATE__ has functions and they're different

This code `@var i = 5 @var(i)` does two things

1 - Create a variable named `i`

2 - Reference a variable named `i`

In Mode `Default No Raw` The variable reference `@var(i)` translates to `@placeholder(@var(i.getType()),@var(i))`

The code `@var(i.getType())` returns type of a variable , which is `int` in this case

so the placeholder invocation becomes `@placeholder(int,@var(i))`

which means

Call a placeholder by the name of `int` using variable `@var(i)` as parameter

`int` placeholder outputs its parameter value in string format

It does that using the following code

```
@runtime.print_string(@var(__param__.toString()))
```

Say if you wanted to write `Kate` or any text before printing every `int` to output

```
@define_placeholder(int,kateint)
    Kate@runtime.print_string(@var(__param__.toString()))
@end_define_placeholder
@var i = 5 @var(i)
```

you define a placeholder named `int` with definition name `kateint` since `int` already exists , By defining the
placeholder you're asking __KATE__ to use it as well

If you'd like to back to previous placeholder

`@use_placeholder(int)`

After this invocation `@var i = 5 @var(i)` , This code will no longer print `Kate` before every int

Now to put everything in your perspective , There are placeholders for all the types __KATE__ supports , Primitives ,
Lists & Objects