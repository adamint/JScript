# JScript Language Specification
## Objects
### Types
*JObject* -> a map that can contain any other object values. Key-value assignments are represented by *VARIABLE_NAME = VALUE*
where VARIABLE_NAME cannot include spaces. Each assignment is separated by a comma (,) and the object begins with { and ends with 
}

An example of a valid object is as follows:
```
{
  object = {
    numbers = [4.3, 5, true],
    hello = "world"
  },
  list = [4.3, 5, false],
  azzerial = "is cool?",
  j44 = nothing
}
```

*JString* -> "value" -> backed value is a **String**. Quotation marks are escaped with \". Semi-colons are not escaped

*JNumber* -> represents a valid number. any type can be manipulated with another, interchangeably.
  * JLong -> backed value is a **Long**
  * JDouble -> backed value is a **Double**
 
 Any operation including a JDouble **will** return a JDouble. For example, 2 + 4.2 will return JDouble(6.2)
 
*JBoolean* -> represents a **Boolean** (false, true)

*JNothing* -> represents a **null** value (nothing)

*JList* -> a list, objects delimiter-separated with a comma (,) and represented with []

### Assignment
Variables are assigned using the equality (=) operator. A variable can be reassigned to any type.
The following operations are translated by the interpreter into an equivalent assignment expression:

1. a += b -> a = a + b
2. a -= b -> a = a - b
3. a *= b -> a = a * b
4. a /= b -> a = a / b
5. a++ -> a = a + 1
6. a-- -> a = a - 1

If an injected variable (non-local) is attempted to be re-assigned, an exception is thrown.

### Arithmetic and mathematical operations
The following operators are enabled. If they're interpreted in a special way, that will be listed in parentheses
1. addition: + (enabled for JString + JString and JList + JObject (field length >= 1)and JObject (field length >= 1) + JObject **also**)
2. subtraction: - (enabled for JObject - JString to remove key/value pair)
3. multiplication: * (enabled for JString * JLong as well, only **in that order**)
4. division: / (both transformed to JDouble. if result is equivalent to an int, transformed to JLong)
5. modulus: %
6. factorial: ! (if JDouble, is transformed into JLong, then JLong.factorial() is called)
7. power: ^ (if JString, equivalent to *. else both values transformed to JDouble, then Math.pow(a, b). if result is equivalent to an int, transformed to JLong)

## Interpreting a program
A String/JObject map is optionally passed into the parser (non-local variables).

**First**, a program string is parsed into a list of statements.

**Next**, each statement is interpreted using the following logic:
1. Variable references replace global AND previously-defined local variable names.
2. An instruction tree is created
TBC