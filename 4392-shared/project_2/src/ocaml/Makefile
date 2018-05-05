N=1
MEASURE_CMD=/usr/bin/time -lp

all: compile

compile:
	corebuild -j 4 -pkg async,unix solver.native
	@echo "Compiled to solver"

clean:
	@rm -rf _build
	@rm solver.native

test: compile
	@echo "Running solver with N=$(N)"
	@./solver.native $(N)
	@make clean

measure: compile
	@echo "Starting measurement"
	@$(MEASURE_CMD) ./solver.native $(N)
	@make clean

