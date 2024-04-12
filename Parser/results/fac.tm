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
 11:     ST  0,-1(5) 	Store local variable fac
* Local variable: x
 12:     ST  0,-2(5) 	Store local variable x
* Statements/Expressions in compound statement
* CallExp: output
* Preparing for output
* Variable Expression: fac
 13:     LD  0,-2(5) 	Load value of fac
 14:    OUT  0,0,0 	output integer value
* start of while loop
* Arithmetic/Logic Operation: (x > 1)
* Variable Expression: x
 15:     LD  0,-3(5) 	Load value of x
 16:    LDC  0,1(0) 	load const
 17:    SUB  0,1,0 	op: >
 18:    JGT  0,2(7) 	branch if true
 19:    LDC  0,0(0) 	false case
 20:    LDA  7,1(7) 	unconditional jump
 21:    LDC  0,1(0) 	true case
* Begin compound statement
* Push new frame or scope marker
* Variable declarations in compound statement
* Statements/Expressions in compound statement
* Arithmetic/Logic Operation: (x - 1)
* Variable Expression: x
 23:     LD  0,-4(5) 	Load value of x
 24:    LDC  0,1(0) 	load const
 25:    SUB  0,0,0 	op: -
* Variable Expression: x
 26:    LDA  0,-6(5) 	Load address of x
 27:     ST  0,-6(5) 	Assign: store value
* Arithmetic/Logic Operation: (fac * x)
* Variable Expression: fac
 28:     LD  0,-7(5) 	Load value of fac
* Variable Expression: x
 29:     LD  0,-8(5) 	Load value of x
 30:    MUL  0,0,0 	op: *
* Variable Expression: fac
 31:    LDA  0,-10(5) 	Load address of fac
 32:     ST  0,-10(5) 	Assign: store value
* Pop frame or scope marker
* End compound statement
 33:    LDA  7,-20(7) 	jump back to the start of the loop
 22:    JEQ  0,11(7) 	Jump to end of loop if condition is false
* end of while loop
 34:    LDC  0,1(0) 	load const
* Variable Expression: fac
 35:    LDA  0,-12(5) 	Load address of fac
 36:     ST  0,-12(5) 	Assign: store value
* CallExp: input
* Preparing for input
 37:     IN  0,0,0 	input integer value
* Variable Expression: x
 38:    LDA  0,-14(5) 	Load address of x
 39:     ST  0,-14(5) 	Assign: store value
* Pop frame or scope marker
* End compound statement
* End of function: main
* End of execution.
 40:   HALT  0,0,0 	

