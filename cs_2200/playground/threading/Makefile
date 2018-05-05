# $^ is dependency
# $@ is target
# @ suppresses output

CFILES = $(wildcard *.c)
OFILES = $(CFILES:%.c=%.o)
TARGET = threading
CFLAGS = -O2 -std=c99
LFLAGS = -lpthread

all: $(OFILES)
	@echo "[LD] Linking $(TARGET)"
	@gcc $(OFILES) -o $(TARGET) $(LFLAGS)       # Links

%.o: %.c
	@gcc -c $^ -o $@ $(CFLAGS)


clean:
	@rm -f $(OFILES) $(TARGET)
	@echo "Removing binaries"

run: all
	@echo "\nRunning $(TARGET)\n"
	@./$(TARGET)
	@echo "\nDONE\n"