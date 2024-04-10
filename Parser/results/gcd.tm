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
* CallExp: gcd
  3:     ST  0,-1(1) 	push argument for gcd
  4:     ST  0,-2(1) 	push argument for gcd
  5:     ST  0,-3(1) 	push argument for output
  6:    LDC  0,10(0) 	load const
  7:     ST  0,1(2) 	Assign: store value
* CallExp: input
  8:     ST  0,1(2) 	Assign: store value
* End compound statement
* End of function: main
  9:   HALT  0,0,0 	End of program execution
* Function declaration: gcd
* Begin compound statement
* Variable declarations in compound statement
* Statements/Expressions in compound statement
 10:     ST  0,0(0) 	op: push left
 11:    LDC  0,0(0) 	load const
 12:     LD  1,0(1) 	op: load left
 13:    JEQ  0,0(7) 	if: jmp to else part
 14:    LDA  7,0(7) 	jmp to end
 13:    JEQ  0,1(7) 	if: jmp to else part (backpatch)
* End compound statement
* End of function: gcd
 14:   HALT  0,0,0 	End of program
* End of execution.
 15:   HALT  0,0,0 	

