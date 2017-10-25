# Language implementation details
First, a command is read from the user, and
checked to see if it has any interpreter pragmas
in it. If so, the interpreter pragma is handled
and we move onto the next command.

Next, the command is prepared for parsing.
This involves 4 steps
1. Convert the command into tokens
2. Split operators from tokens. This means
	converting tokens like 2+2 into the three tokens
	2, + and 2
3. Deaffix tokens. This means deattaching brackets
	and parenthesis from their attached tokens.
4. Remove blank tokens

Next, is parsing. This is just a modified version
of the shunting-yard algorithm, with the
main modification being it properly handles
multiple nesting levels of parenthesis

Then, the AST is created from the parsed
string.