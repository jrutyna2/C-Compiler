JAVA=java
JAVAC=javac
JFLEX=jflex
CUP=cup
CLASSPATH:=/usr/share/java/cup.jar:.:./absyn/classes:./build:./symbols/classes:./utils/classes
ABSYN=absyn/classes
BUILD=build
SYMBOLS=symbols/classes # Assuming we want to compile symbols to a separate folder
TESTDIR=tests
.PHONY: test testa tests testall runc
file:=1
UTILS=utils/classes

all: build

buildgen: Lexer.java parser.java sym.java
	mkdir -p $(BUILD)
	$(JAVAC) -cp $(CLASSPATH) -d $(BUILD) absyn/*.java
	$(JAVAC) -cp $(CLASSPATH) -d $(SYMBOLS) symbols/CodeGenerator.java
	$(JAVAC) -cp $(CLASSPATH) -d $(BUILD) symbols/*.java
	$(JAVAC) -cp $(CLASSPATH) -d $(BUILD) utils/*.java
	$(JAVAC) -cp $(CLASSPATH) -d $(BUILD) parser.java sym.java Lexer.java ShowTreeVmakisitor.java Scanner.java Main.java

testcodegen:
	@$(JAVA) -cp $(CLASSPATH) Main $(TESTDIR)/$(file).cm -c
	
buildsyn: Lexer.java parser.java sym.java
	mkdir -p $(BUILD)
	$(JAVAC) -cp $(CLASSPATH) -d $(BUILD) parser.java sym.java Lexer.java ShowTreeVisitor.java Scanner.java Main.java

cleansyn: 	
	rm -f parser.java Lexer.java sym.java
	rm -rf $(BUILD)

rbsyn: cleansyn buildsyn

buildsym: 
	$(JAVAC) -cp $(CLASSPATH) -d $(BUILD) symbols/*.java

rbsyms: buildsym
	@$(JAVA) -cp $(CLASSPATH) $(TESTDIR)/$(file).cm -s # Test with -s option: make rbsyms file=gcd

tsyna: rbsyn
	@$(JAVA) -cp $(CLASSPATH) Main -a $(TESTDIR)/$(file).cm # Test with -a option: make tsyna

tsyns: rbsyn
	@$(JAVA) -cp $(CLASSPATH) Main -s $(TESTDIR)/$(file).cm # Test with -s option: make tsyns

build: Lexer.java parser.java sym.java
		mkdir -p $(BUILD)
		$(JAVAC) -cp $(CLASSPATH) -d $(BUILD) absyn/*.java
		$(JAVAC) -cp $(CLASSPATH) -d $(BUILD) symbols/*.java
		$(JAVAC) -cp $(CLASSPATH) -d $(BUILD) utils/*.java
		$(JAVAC) -cp $(CLASSPATH) -d $(BUILD) parser.java sym.java Lexer.java ShowTreeVisitor.java Scanner.java Main.java

# build: Lexer.java parser.java sym.java
# 	mkdir -p $(ABSYN)
# 	mkdir -p $(BUILD)
# 	mkdir -p $(SYMBOLS) # Create the symbols classes directory
# 	$(JAVAC) -cp $(CLASSPATH) -d $(ABSYN) absyn/*.java
# 	$(JAVAC) -cp $(CLASSPATH) -d $(SYMBOLS) symbols/*.java # Compile the symbols directory
# 	$(JAVAC) -cp $(CLASSPATH) -d $(UTILS) utils/*.java
# 	$(JAVAC) -cp $(CLASSPATH) -d $(BUILD) parser.java sym.java Lexer.java ShowTreeVisitor.java Scanner.java Main.java

Lexer.java: C.flex
	$(JFLEX) C.flex

parser.java: C.cup
	$(CUP) -expect 3 C.cup

# clean:
# 	rm -f parser.java Lexer.java sym.java *.class *~
# 	rm -rf $(ABSYN) $(BUILD) $(SYMBOLS)

clean:
	rm -f parser.java Lexer.java sym.java *.class *~
	rm -rf $(BUILD)

rebuild: clean all

testa: 
	@$(JAVA) -cp $(CLASSPATH) Main $(TESTDIR)/$(file).cm -a # Test with -a option: make testa file=ifwhile/test1

tests:
	@$(JAVA) -cp $(CLASSPATH) Main $(TESTDIR)/$(file).cm -s # Test with -s option

testc:
	@$(JAVA) -cp $(CLASSPATH) Main $(TESTDIR)/$(file).cm -c # Test with -s option

retesta: rebuild
	@$(JAVA) -cp $(CLASSPATH) Main $(TESTDIR)/$(file).cm -a # Test with -a option

retests: rebuild
	@$(JAVA) -cp $(CLASSPATH) Main $(TESTDIR)/$(file).cm -s # Test with -s option: make retests file=1

retestc: rebuild
	@$(JAVA) -cp $(CLASSPATH) Main $(TESTDIR)/$(file).cm -c # Test with -s option

cleanp:
	rm -f parser.java

ta: parser.java
	@$(JAVA) -cp $(CLASSPATH) Main -a $(TESTDIR)/$(file).cm # Test with -a option: make ta

ts: parser.java
	@$(JAVA) -cp $(CLASSPATH) Main -s $(TESTDIR)/$(file).cm # Test with -s option: make ts

rta: cleanp parser.java
	@$(JAVA) -cp $(CLASSPATH) Main -a $(TESTDIR)/$(file).cm # Test with -a option: make rta

rts: cleanp parser.java
	@$(JAVA) -cp $(CLASSPATH) Main -s $(TESTDIR)/$(file).cm # Test with -s option: make rts

testall: rebuild
	for file in $(TESTDIR)/*.cm; do \
		echo Running $$file; \
		$(JAVA) -cp $(CLASSPATH) Main $$file; \
		echo; \
	done

testalls: rebuild # make testalls TESTDIR=test/assign
	for file in $(TESTDIR)/*.cm; do \
		echo Running $$file; \
		$(JAVA) -cp $(CLASSPATH) Main -s $$file; \
		echo; \
	done

talls: # test all files in dir w/o rebuild: make talls TESTDIR=test/assign
	for file in $(TESTDIR)/*.cm; do \
		echo Running $$file; \
		$(JAVA) -cp $(CLASSPATH) Main -s $$file; \
		echo; \
	done

runc: testall clean
