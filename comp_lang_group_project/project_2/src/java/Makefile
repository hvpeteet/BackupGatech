MAIN_CLASS=Solver
N=1
MEASURE_CMD=/usr/bin/time -lp

all: jar

compile:
	@javac *.java
	@echo "Compiled java files"

jar: compile
	@echo "Creating solver.jar"
	@jar cfe solver.jar $(MAIN_CLASS) *.class
	@make clean

clean:
	@echo "Removing Class files"
	@rm *.class

test: jar
	@echo "Running solver with N=$(N)"
	@java -jar solver.jar $(N)

measure: jar
	@echo "Starting measurement"
	@$(MEASURE_CMD) java -jar solver.jar $(N)

