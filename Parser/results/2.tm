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
  3:     ST  0,0(0) 	op: push left
  4:     LD  1,0(1) 	op: load left
  5:    ADD  0,1,0 	op: +
  6:     ST  0,1(2) 	Assign: store value
  7:    LDC  0,10(0) 	load const
  8:     ST  0,1(2) 	Assign: store value
  9:    LDC  0,5(0) 	load const
 10:     ST  0,1(2) 	Assign: store value
* End compound statement
* End of function: main
 11:   HALT  0,0,0 	End of program execution
 12:   HALT  0,0,0 	End of program
* End of execution.
 13:   HALT  0,0,0 	

