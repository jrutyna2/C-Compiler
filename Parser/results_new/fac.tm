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
* Local variable: x
* Statements/Expressions in compound statement
* CallExp: output
* Variable Expression: fac
 11:     LD  0,-2(5) 	Load value of fac
 12:    OUT  0,0,0 	output integer value
* start of while loop
* Variable Expression: x
 13:     LD  0,-3(5) 	Load value of x
 14:     ST  0,0(0) 	op: push left
 15:    LDC  0,1(0) 	load const
 16:     LD  1,0(1) 	op: load left
 17:    SUB  0,1,0 	op: >
 18:    JGT  0,2(7) 	branch if true
 19:    LDC  0,0(0) 	false case
 20:    LDA  7,1(7) 	unconditional jump
 21:    LDC  0,1(0) 	true case
* Begin compound statement
* Push new frame or scope marker
* Variable declarations in compound statement
* Statements/Expressions in compound statement
* Variable Expression: x
 23:     LD  0,-4(5) 	Load value of x
 24:     ST  0,0(0) 	op: push left
 25:    LDC  0,1(0) 	load const
 26:     LD  1,0(1) 	op: load left
 27:    SUB  0,1,0 	op: -
* Variable Expression: x
 28:    LDA  0,-6(5) 	Load address of x
 29:     ST  0,-6(5) 	Assign: store value
* Variable Expression: fac
 30:     LD  0,-7(5) 	Load value of fac
 31:     ST  0,0(0) 	op: push left
* Variable Expression: x
 32:     LD  0,-8(5) 	Load value of x
 33:     LD  1,0(1) 	op: load left
 34:    MUL  0,1,0 	op: *
* Variable Expression: fac
 35:    LDA  0,-10(5) 	Load address of fac
 36:     ST  0,-10(5) 	Assign: store value
* Pop frame or scope marker
* End compound statement
 37:    LDA  7,-26(7) 	jump back to the start of the loop
 22:    JEQ  0,15(7) 	Jump to end of loop if condition is false
* end of while loop
 38:    LDC  0,1(0) 	load const
* Variable Expression: fac
 39:    LDA  0,-12(5) 	Load address of fac
 40:     ST  0,-12(5) 	Assign: store value
* CallExp: input
 41:     IN  0,0,0 	input integer value
* Variable Expression: x
 42:    LDA  0,-14(5) 	Load address of x
 43:     ST  0,-14(5) 	Assign: store value
* Pop frame or scope marker
* End compound statement
* End of function: main
 44:   HALT  0,0,0 	End of program execution
 45:   HALT  0,0,0 	End of program execution
* End of execution.
 46:   HALT  0,0,0 	

