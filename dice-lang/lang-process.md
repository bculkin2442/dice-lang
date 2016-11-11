# Top level reader loop
First, a command is read in from the user under a prompt

Next, it is checked if the first token of the command (split on " ")
corresponds to an action name
* If it does, the corresponding action is executed, and the next command is
  read
* If it doesn't, the command is parsed as a language command and executed

This continues until the command "quit" (non case-sensitive) is read

# Known actions
The currently implemented actions are:
* env: print out the contents of the enviroment. Takes no arguments
* inline: inline variables out of the definition of specified variables. Takes
  at least two arguments
  * The first is the name of the variable to do inlining in
  * The second is the name of the variable to bind the inlined expression to
  * The third and following are the names of the variables to inline in the
	variable you are inlining. If you don't give any, it will inline every
	variable

## Details on inlining
The way the inlining process works is simple. The tree for the variable to be
inlined is read, and then each variable reference is inlined if it is marked as
one of the variable references to inline. This only occurs one layer deep

# Parsing language commands
Once it is decided to parse a command as a language command, it goes through
four steps:
* Preparation
* AST Building
* AST Transformation
* AST Evaluation

# Command preparation
Command preparation means turning the raw space-seperated tokens into tokens
suitable for feeding the AST builder. It involves the following steps:
* The first is operator expansion, which turns a token like 4+4 into the three
  tokens 4, +, and 4
* The next is token deaffixation which turns a token like (4 into the tokens (
  and 4. However, ((4 will become (( 4, not ( ( 4, because (( is a different
  nesting level than (
* Next, any blank tokens that have been created as a result of the process are
  disposed of
* Finally, the entire expression is shunted, turning it from infix notation to
  postfix notation. The only real special thing about this is that (( is a
  different nesting level than (. The token precedence levels are (sorted from
  lower to higher precedence): 
  * Math precedence
   * + & -
   * * & /
   * 
  * Dice precedence
   * d
   * c
  * Expression precedence
   * =>
   * :=

# AST Building
AST building is a slightly more complex process, as the tree is traversed
twice: once to build the tree, and then again to convert the tokens from
strings into data tokens. The only really finicky part about the first tree is
arrays, because arrays are still in infix form at this point.
