import java.nio.ByteBuffer;

// You shouldn't have to modify this class at all
// But take a look to see how it works

public class Memory {

    private int MAXSIZE; // the max that can be stored
    private int[] intMem; // Used for storing all values from all int arrays
    private float[] floatMem; // Used for storing all values from all float arrays
    private int allocPointerInt = 0;
    private int allocPointerFloat = 0;

    public Memory() {
        this.MAXSIZE = 10000;
        intMem = new int[MAXSIZE];
        floatMem = new float[MAXSIZE];
    }

    public Memory(int MAXSIZE) {
        this.MAXSIZE = MAXSIZE;
        intMem = new int[MAXSIZE];
        floatMem = new float[MAXSIZE];
    }

    // allocates memory for a new int array
    // sets all elements to the specified default value
    // returns the base address at which the array was allocated
    public int initIntArray(int size, int defaultValue) {
        for (int i = 0; i < size; i++) {
            intMem[allocPointerInt + i] = defaultValue;
        }
        int ret = allocPointerInt;
        allocPointerInt += size;
        return ret;
    }

    // allocates memory for a new float array
    // sets all elements to the specified default value
    // returns the base address at which the array was allocated
    public int initFloatArray(int size, float defaultValue) {
        for (int i = 0; i < size; i++) {
            floatMem[allocPointerInt + i] = defaultValue;
        }
        int ret = allocPointerFloat;
        allocPointerFloat += size;
        return ret;
    }

    public void setInt(int base, int index, int value) {
        intMem[base + index] = value;
    }

    public void setFloat(int base, int index, float value) {
        floatMem[base + index] = value;
    }

    public int getInt(int base, int index) {
        return intMem[base + index];
    }
    public float getFloat(int base, int index) {
        return floatMem[base + index];
    }
}
