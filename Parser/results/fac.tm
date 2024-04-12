* C-Minus Compilation to TM Code
* File: results/fac.tm
* Standard prelude:
  0:     LD  6,0(0) 	load gp with maxaddress
  1:    LDA  5,0(6) 	copy to gp to fp
  2:     ST  0,0(0) 	clear location 0
* Jump around i/o routines here
* code for input routine
  4:     ST  0,-1(5) 	store return
  5:     IN  0,0,0 	input
  6:     LD  7,-1(5) 	return to caller
* code for output routine
  7:     ST  0,-1(5) 	store return
  8:     LD  0,-2(5) 	load output value
  9:    OUT  0,0,0 	output
 10:     LD  7,-1(5) 	return to caller
  3:    LDA  7,7(7) 	jump around i/o code
* End of standard prelude.
* Start of main function
* Begin compound statement
* Push new frame or scope marker
* Variable declarations in compound statement
* Local variable: fac
 11:    LDC  0,0(0) 	Optionally init fac to 0
 12:     ST  0,-1(5) 	Store local variable fac
* Local variable: x
 13:    LDC  0,0(0) 	Optionally init x to 0
 14:     ST  0,-2(5) 	Store local variable x
* Statements/Expressions in compound statement
* CallExp: output
* Variable Expression: fac
 15:     LD  0,-2(5) 	Load value of fac
 16:    OUT  0,0,0 	output integer value
* start of while loop
* Arithmetic/Logic Operation: (x > 1)
* Variable Expression: x
 17:     LD  0,-3(5) 	Load value of x
 18:    LDC  0,1(0) 	load const
 19:    SUB  0,1,0 	op: >
 20:    JGT  0,2(7) 	branch if true
 21:    LDC  0,0(0) 	false case
 22:    LDA  7,1(7) 	unconditional jump
 23:    LDC  0,1(0) 	true case
* Begin compound statement
* Push new frame or scope marker
* Variable declarations in compound statement
* Statements/Expressions in compound statement
* Arithmetic/Logic Operation: (x - 1)
* Variable Expression: x
 25:     LD  0,-4(5) 	Load value of x
 26:    LDC  0,1(0) 	load const
 27:    SUB  0,0,0 	op: -
* Variable Expression: x
 28:    LDA  0,-6(5) 	Load address of x
 29:     ST  0,-6(5) 	Assign: store value
* Arithmetic/Logic Operation: (fac * x)
* Variable Expression: fac
 30:     LD  0,-7(5) 	Load value of fac
* Variable Expression: x
 31:     LD  0,-8(5) 	Load value of x
 32:    MUL  0,0,0 	op: *
* Variable Expression: fac
 33:    LDA  0,-10(5) 	Load address of fac
 34:     ST  0,-10(5) 	Assign: store value
* Pop frame or scope marker
* End compound statement
 35:    LDA  7,-20(7) 	jump back to the start of the loop
 24:    JEQ  0,11(7) 	Jump to end of loop if condition is false
* end of while loop
 36:    LDC  0,1(0) 	load const
* Variable Expression: fac
 37:    LDA  0,-12(5) 	Load address of fac
 38:     ST  0,-12(5) 	Assign: store value
* CallExp: input
 39:     IN  0,0,0 	input integer value
* Variable Expression: x
 40:    LDA  0,-14(5) 	Load address of x
 41:     ST  0,-14(5) 	Assign: store value
* Pop frame or scope marker
* End compound statement
* End of function: main
* End of execution.
 42:   HALT  0,0,0 	

