* Standard prelude:
  0:     LD  6,0,0 	load gp with maxaddress
  1:    LDA  5,0,6 	copy to gp to fp
  2:     ST  0,0,0 	clear location 0
* End of standard prelude.
* C- compilation to TM code
* Function declaration: run
* Begin compound statement
* Variable declarations in compound statement
* Statements/Expressions in compound statement
* start of return
  3:    LDC  0,1(0) 	load const
  4:     LD  7,-1(2) 	Load return address and jump to caller
* end of return
* End compound statement
* End of function: run
  5:   HALT  0,0,0 	End of program
* End of execution.
  6:   HALT  0,0,0 	

