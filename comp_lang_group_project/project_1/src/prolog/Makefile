N=1
MEASURE_CMD=/usr/bin/time -lp

all: compile

compile:
	@echo "Prolog Does not compile so..."

clean:
	@echo "Nothing to clean ... "

test:
	@echo "Running solver with N=$(N)"
	@swipl solver.pl $(N)

measure:
	@echo "Starting measurement"
	@$(MEASURE_CMD) swipl solver.pl $(N)

