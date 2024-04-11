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
* Variable declarations in compound statement
* Statements/Expressions in compound statement
* CallExp: output
 11:    OUT  0,0,0 	output integer value
 12:     ST  0,-1(1) 	push argument for output
* start of while loop
 13:     ST  0,0(0) 	op: push left
 14:    LDC  0,1(0) 	load const
 15:     LD  1,0(1) 	op: load left
 16:    SUB  0,1,0 	op: >
 17:    JGT  0,2(7) 	branch if true
 18:    LDC  0,0(0) 	false case
 19:    LDA  7,1(7) 	unconditional jump
 20:    LDC  0,1(0) 	true case
* Begin compound statement
* Variable declarations in compound statement
* Statements/Expressions in compound statement
 22:     ST  0,0(0) 	op: push left
 23:    LDC  0,1(0) 	load const
 24:     LD  1,0(1) 	op: load left
 25:    SUB  0,1,0 	op: -
 26:     ST  0,-1(5) 	Assign: store value
 27:     ST  0,0(0) 	op: push left
 28:     LD  1,0(1) 	op: load left
 29:    MUL  0,1,0 	op: *
 30:     ST  0,-2(5) 	Assign: store value
* End compound statement
 31:    LDA  7,-20(7) 	jump back to the start of the loop
 21:    JEQ  0,9(7) 	Jump to end of loop if condition is false
* end of while loop
 32:    LDC  0,1(0) 	load const
 33:     ST  0,-3(5) 	Assign: store value
* CallExp: input
 34:     IN  0,0,0 	input integer value
 35:     ST  0,-4(5) 	Assign: store value
* End compound statement
* End of function: main
 36:   HALT  0,0,0 	End of program execution
 37:   HALT  0,0,0 	End of program execution
* End of execution.
 38:   HALT  0,0,0 	

