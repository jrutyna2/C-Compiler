* C-Minus Compilation to TM Code
* File: results/fac.tm
* Standard prelude:
  0:     LD  6,0(0) 	load gp with maxaddress
  1:    LDA  5,0(6) 	copy to gp to fp
  2:     ST  0,0(0) 	clear location 0
* End of standard prelude.
* code for input routine
  4:     ST  0,-1(5) 	store return
  5:     IN  0,0,0 	input
  6:     LD  7,-1(5) 	return to caller
* code for output routine
  7:     ST  0,-1(5) 	store return
  8:     LD  0,-2(5) 	load output value
  9:    OUT  0,0,0 	output
 10:     LD  7,-1(5) 	return to caller
  3:    LDA  7,7(7) 	Jump around I/O routines to the start of the main program
* C- compilation to TM code
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
* Variable Expression: x
 17:     LD  0,-3(5) 	Load value of x
 18:     ST  0,0(0) 	op: push left
 19:    LDC  0,1(0) 	load const
 20:     LD  1,0(1) 	op: load left
 21:    SUB  0,1,0 	op: >
 22:    JGT  0,2(7) 	branch if true
 23:    LDC  0,0(0) 	false case
 24:    LDA  7,1(7) 	unconditional jump
 25:    LDC  0,1(0) 	true case
* Begin compound statement
* Push new frame or scope marker
* Variable declarations in compound statement
* Statements/Expressions in compound statement
* Variable Expression: x
 27:     LD  0,-4(5) 	Load value of x
 28:     ST  0,0(0) 	op: push left
 29:    LDC  0,1(0) 	load const
 30:     LD  1,0(1) 	op: load left
 31:    SUB  0,1,0 	op: -
* Variable Expression: x
 32:    LDA  0,-6(5) 	Load address of x
 33:     ST  0,-6(5) 	Assign: store value
* Variable Expression: fac
 34:     LD  0,-7(5) 	Load value of fac
 35:     ST  0,0(0) 	op: push left
* Variable Expression: x
 36:     LD  0,-8(5) 	Load value of x
 37:     LD  1,0(1) 	op: load left
 38:    MUL  0,1,0 	op: *
* Variable Expression: fac
 39:    LDA  0,-10(5) 	Load address of fac
 40:     ST  0,-10(5) 	Assign: store value
* Pop frame or scope marker
* End compound statement
 41:    LDA  7,-26(7) 	jump back to the start of the loop
 26:    JEQ  0,15(7) 	Jump to end of loop if condition is false
* end of while loop
 42:    LDC  0,1(0) 	load const
* Variable Expression: fac
 43:    LDA  0,-12(5) 	Load address of fac
 44:     ST  0,-12(5) 	Assign: store value
* CallExp: input
 45:     IN  0,0,0 	input integer value
* Variable Expression: x
 46:    LDA  0,-14(5) 	Load address of x
 47:     ST  0,-14(5) 	Assign: store value
* Pop frame or scope marker
* End compound statement
* End of function: main
 48:   HALT  0,0,0 	End of program execution
 49:   HALT  0,0,0 	End of program execution
* End of execution.
 50:   HALT  0,0,0 	

