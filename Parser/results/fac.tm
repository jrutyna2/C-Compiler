* Standard prelude:
  0:     LD  6,0,0 	load gp with maxaddress
  1:    LDA  5,0,6 	copy to gp to fp
  2:     ST  0,0,0 	clear location 0
* End of standard prelude.
* C- compilation to TM code
* Start of main function
* Begin compound statement
* Variable declarations in compound statement
* Statements/Expressions in compound statement
* CallExp: output
  3:    OUT  0,0,0 	output integer value
  4:     ST  0,-1(1) 	push argument for output
* start of while loop
  5:     ST  0,0(0) 	op: push left
  6:    LDC  0,1(0) 	load const
  7:     LD  1,0(1) 	op: load left
* Begin compound statement
* Variable declarations in compound statement
* Statements/Expressions in compound statement
  9:     ST  0,0(0) 	op: push left
 10:    LDC  0,1(0) 	load const
 11:     LD  1,0(1) 	op: load left
 12:    SUB  0,1,0 	op: -
 13:     ST  0,1(2) 	Assign: store value
 14:     ST  0,0(0) 	op: push left
 15:     LD  1,0(1) 	op: load left
 16:    MUL  0,1,0 	op: *
 17:     ST  0,1(2) 	Assign: store value
* End compound statement
 18:    LDA  7,-15(7) 	jump back to the start of the loop
  8:    JEQ  0,9(7) 	Jump to end of loop if condition is false
* end of while loop
 19:    LDC  0,1(0) 	load const
 20:     ST  0,1(2) 	Assign: store value
* CallExp: input
 21:     IN  0,0,0 	input integer value
 22:     ST  0,1(2) 	Assign: store value
* End compound statement
* End of function: main
 23:   HALT  0,0,0 	End of program execution
 24:   HALT  0,0,0 	End of program
* End of execution.
 25:   HALT  0,0,0 	

