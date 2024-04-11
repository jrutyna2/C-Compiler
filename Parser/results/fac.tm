* C-Minus Compilation to TM Code
* File: results/fac.tm
* * Standard prelude:
  0:     LD  0,0(0) 	load gp with maxaddress
  1:    LDA  2,0(0) 	copy to gp to fp
  2:     ST  0,0(0) 	clear location 0
* End of standard prelude.
* code for input routine
  4:     ST  0,-1(2) 	store return
  5:     IN  0,0,0 	input
  6:     LD  7,-1(2) 	return to caller
* code for output routine
  7:     ST  0,-1(2) 	store return
  8:     LD  0,-2(2) 	load output value
  9:    OUT  0,0,0 	output
 10:     LD  7,-1(2) 	return to caller
  3:    LDA  7,7(7) 	Jump around I/O routines to the start of the main program
* C- compilation to TM code
* Start of main function
* Begin compound statement
* Variable declarations in compound statement
* Statements/Expressions in compound statement
* CallExp: output
 11:    OUT  0,0,0 	output integer value
 12:     ST  0,-1(1) 	push argument for output
* start of while loop
 13:     ST  0,0(0) 	op: push left
 14:    LDC  0,1(0) 	load const
 15:     LD  1,0(1) 	op: load left
* Begin compound statement
* Variable declarations in compound statement
* Statements/Expressions in compound statement
 17:     ST  0,0(0) 	op: push left
 18:    LDC  0,1(0) 	load const
 19:     LD  1,0(1) 	op: load left
 20:    SUB  0,1,0 	op: -
 21:     ST  0,1(2) 	Assign: store value
 22:     ST  0,0(0) 	op: push left
 23:     LD  1,0(1) 	op: load left
 24:    MUL  0,1,0 	op: *
 25:     ST  0,1(2) 	Assign: store value
* End compound statement
 26:    LDA  7,-15(7) 	jump back to the start of the loop
 16:    JEQ  0,9(7) 	Jump to end of loop if condition is false
* end of while loop
 27:    LDC  0,1(0) 	load const
 28:     ST  0,1(2) 	Assign: store value
* CallExp: input
 29:     IN  0,0,0 	input integer value
 30:     ST  0,1(2) 	Assign: store value
* End compound statement
* End of function: main
 31:   HALT  0,0,0 	End of program execution
 32:   HALT  0,0,0 	End of program execution
* End of execution.
 33:   HALT  0,0,0 	

