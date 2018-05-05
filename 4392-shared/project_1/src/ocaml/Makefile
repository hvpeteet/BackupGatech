N=1
MEASURE_CMD=/usr/bin/time -lp

all: compile

compile:
	@ocamlc -o solver solver.ml
	@echo "Compiled to solver"

clean:
	@rm solver.cmi
	@rm solver.cmo
	@rm solver

test: compile
	@echo "Running solver with N=$(N)"
	@./solver $(N)
	@make clean

measure: compile
	@echo "Starting measurement"
	@$(MEASURE_CMD) ./solver $(N)
	@make clean

