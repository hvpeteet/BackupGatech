INPUT=solver.go
OUTPUT=solver
N=1
MEASURE_CMD=/usr/bin/time -lp

all: compile

compile:
	@echo "Building $(INPUT)"
	@go build $(INPUT)

clean:
	@echo "Removing compiled files"
	@go clean

test: compile
	@echo "Running solver with N=$(N)"
	@./$(OUTPUT) $(N)

measure: compile
	@echo "Starting measurement"
	@$(MEASURE_CMD) ./$(OUTPUT) $(N)
