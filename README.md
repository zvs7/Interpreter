# Interpreter
The following defines a simple language, in which a program consists of assignments and each variable is assumed to be of the integer type. For the sake of simplicity, only operators that give integer values are included. Write an interpreter for the language in a language of your choice. Your interpreter should be able to do the following for a given program: (1) detect syntax errors; (2) report uninitialized variables; and (3) perform the assignments if there is no error and print out the values of all the variables after all the assignments are done.

Program:
	Assignment*

Assignment:
	Identifier = Exp;

Exp: 
	Exp + Term | Exp - Term | Term

Term:
	Term * Fact  | Fact

Fact:
	( Exp ) | - Fact | + Fact | Literal | Identifier

Identifier:
     	Letter [Letter | Digit]*

Letter:
	a|...|z|A|...|Z|_

Literal:
	0 | NonZeroDigit Digit*
		
NonZeroDigit:
	1|...|9

Digit:
	0|1|...|9

Sample inputs and outputs
Input 1
x = 001;

Output 1
error

Input 2
x_2 = 0;

Output 2
x_2 = 0

Input 3
x = 0
y = x;
z = ---(x+y);

Output 3
error

Input 4
x = 1;
y = 2;
z = ---(x+y)*(x+-y);

Output 4
x = 1
y = 2
z = 3
