# Dice-Lang Language Description
Dice lang was originally just a program for rolling
patterns of dice. However, through some effort
and pushing a shunting-yard parser farther than
it probably should have gone, it became a language.
It's still missing some things, but its getting there

## Basic Syntax
You can use it like a 4-function calculator.
```
1+1
-> 2
1+1
2+2*2+2
-> 8
```
However, we don't support floating point numbers or math
```
1.1
-> ERROR: Floating point literals are not supported
10/3
-> 3
```
We do, however, support dice literals
```
1d6
-> 6
1d6
-> 3
```
These can be treated as numbers, but won't get turned into
numbers until you actually ask them to turn into numbers.

## Variables and Assignment
There are variables, and you can assign things to them
```
test := 1
-> 1
```
When you assign a variable, its current value is mentioned.
To make sure that dice behave correctly, you can bind
them to a variable
```
die := 1d6
-> 5
die
-> 3
```
There exists a meta-variable 'last' whose value is always the
result of the last expression.
```
test := 1d6*2d8
-> 9
last
-> 30
```
We also have let, for binding things in the context of an
expression. However, let isn't quite working at the moment
## Arrays
