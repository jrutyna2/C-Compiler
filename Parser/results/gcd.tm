* Standard prelude:
  0:     LD  6,0,0 	load gp with maxaddress
  1:    LDA  5,0,6 	copy to gp to fp
  2:     ST  0,0,0 	clear location 0
* End of standard prelude.
* C- compilation to TM code
* Function declaration: gcd
* Begin compound statement
* Variable declarations in compound statement
* Statements/Expressions in compound statement
  3:     ST  0,0(0) 	op: push left
  4:    LDC  0,0(0) 	load const
  5:     LD  1,0(1) 	op: load left
  6:    JEQ  0,0(7) 	if: jmp to else part
  7:    LDA  7,0(7) 	jmp to end
  6:    JEQ  0,1(7) 	if: jmp to else part (backpatch)
* End compound statement
* End of function: gcd
  7:     LD  7,-1(2) 	Return to caller
* Start of main function
* Begin compound statement
* Variable declarations in compound statement
* Statements/Expressions in compound statement
* CallExp: output
* CallExp: gcd
  8:     ST  0,-1(1) 	push argument for gcd
  9:     ST  0,-2(1) 	push argument for gcd
* Function call to gcd
 10:    LDA  7,2(7) 	jump to function gcd
* Function call gcd return
 11:    LDA  1,2(1) 	adjust sp back after function call gcd
 12:    OUT  0,0,0 	output integer value
 13:    LDC  0,10(0) 	load const
 14:     ST  0,1(2) 	Assign: store value
* CallExp: input
 15:     IN  0,0,0 	read integer value
 16:     ST  0,1(2) 	Assign: store value
* End compound statement
* End of function: main
 17:   HALT  0,0,0 	End of program execution
 18:   HALT  0,0,0 	End of program
* End of execution.
 19:   HALT  0,0,0 	

