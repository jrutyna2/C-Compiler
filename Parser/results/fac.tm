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
* Local variable: x
 11:     ST  0,-1(5) 	Store local variable x
* Local variable: fac
 12:     ST  0,-2(5) 	Store local variable fac
* Statements/Expressions in compound statement
* CallExp: input
* Preparing for input
 13:     IN  0,0,0 	input integer value
* Variable Expression: x
 14:    LDA  0,-3(5) 	Load address of x
 15:     ST  0,-3(5) 	Assign: store value
 16:    LDC  0,1(0) 	load const
* Variable Expression: fac
 17:    LDA  0,-5(5) 	Load address of fac
 18:     ST  0,-5(5) 	Assign: store value
* start of while loop
* Arithmetic/Logic Operation: (x > 1)
* Variable Expression: x
 19:     LD  0,-6(5) 	Load value of x
 20:    LDC  0,1(0) 	load const
 21:    SUB  0,1,0 	op: >
 22:    JGT  0,2(7) 	branch if true
 23:    LDC  0,0(0) 	false case
 24:    LDA  7,1(7) 	unconditional jump
 25:    LDC  0,1(0) 	true case
* Begin compound statement
* Push new frame or scope marker
* Variable declarations in compound statement
* Statements/Expressions in compound statement
* Arithmetic/Logic Operation: (fac * x)
* Variable Expression: fac
 27:     LD  0,-7(5) 	Load value of fac
* Variable Expression: x
 28:     LD  0,-8(5) 	Load value of x
 29:    MUL  0,0,0 	op: *
* Variable Expression: fac
 30:    LDA  0,-10(5) 	Load address of fac
 31:     ST  0,-10(5) 	Assign: store value
* Arithmetic/Logic Operation: (x - 1)
* Variable Expression: x
 32:     LD  0,-11(5) 	Load value of x
 33:    LDC  0,1(0) 	load const
 34:    SUB  0,0,0 	op: -
* Variable Expression: x
 35:    LDA  0,-13(5) 	Load address of x
 36:     ST  0,-13(5) 	Assign: store value
* Pop frame or scope marker
* End compound statement
 37:    LDA  7,-20(7) 	jump back to the start of the loop
 26:    JEQ  0,11(7) 	Jump to end of loop if condition is false
* end of while loop
* CallExp: output
* Preparing for output
* Variable Expression: fac
 38:     LD  0,-14(5) 	Load value of fac
 39:    OUT  0,0,0 	output integer value
* Pop frame or scope marker
* End compound statement
* End of function: main
* End of execution.
 40:   HALT  0,0,0 	

