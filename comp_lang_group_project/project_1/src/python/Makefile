N=1
MEASURE_CMD=/usr/bin/time -lp

all: compile

compile:
	@echo "Nothing to compile"

clean:
	@echo "nothing to clean"

test:
	@echo "Running solver with N=$(N)"
	@python3 solver.py $(N)
	@make clean

measure:
	@echo "Starting measurement"
	@$(MEASURE_CMD) python3 solver.py $(N)
	@make clean

