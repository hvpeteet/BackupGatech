import java.util.EmptyStackException;
import java.util.Scanner;
import java.util.Stack;

// The state is composed of a stack of frames
// where a frame is simply a key-value map.
// Every time a function is called, you should create a new frame
// using the push() method. When a function is exited, pop() the frame.
// You can store whatever you feel is appropriate within each frame.
// See the sample partial interpreter for an example of how it works
//
// The state is initialized with a single frame
// This frame is called the global frame and is accessed
// if a request to the top frame fails.
//
// Memory mem is used for allocating arrays. All array values are stored
// loads and stores from arrays are implemented via reads/writes to
// mem's internal data structures. HOWEVER, the address of arrays are
// stored NOT in mem, but in the stack frame. You can query the address
// of an array by calling getInt("ARRAY_NAME")


public class State {

    private Stack<Frame> stack  = new Stack<>();
    private Frame global;
    private Memory mem = new Memory();

    // Creates new state with a single stack frame
    // and sets the globally accessible frame to this frame
    public State() {
        push();
        global = peek();
    }

    // Returns true if the current frame
    // is the global frame
    public boolean inGlobalScope() {
        return global == peek();
    }

    // Pushes a new frame onto the stack
    public void push() {
        stack.push(new Frame());
    }

    // Pops a frame off the stack
    // Returns the state
    public State pop() throws EmptyStackException {
        stack.pop();
        return this;
    }

    // Returns the frame on top of the stack
    private Frame peek() throws EmptyStackException {
        return stack.peek();
    }

    // Returns the int value binded to given key in top frame
    // If not found in top frame, returns value binded in global frame
    // If not found in global frame, returns null
    public Integer getInt(String key) {
        Integer ret = (Integer)peek().get(key);
        return (ret == null)
            ? (Integer)global.get(key)
            : ret;
    }

    // Returns the float value binded to given key in top frame
    // If not found in top frame, returns value binded in global frame
    // If not found in global frame, returns null
    public Float getFloat(String key) {
        Float ret = (Float)peek().get(key);
        return (ret == null)
                ? (Float)global.get(key)
                : ret;
    }

    // Returns the String value binded to given key in top frame
    // If not found in top frame, returns value binded in global frame
    // If not found in global frame, returns null
    public String getString(String key) {
        String ret = (String)peek().get(key);
        return (ret == null)
                ? (String)global.get(key)
                : ret;
    }

    // Inserts given key value pair
    // Set local to false to bind to global frame
    // Set local to true to bind to top frame
    // Returns the state
    public State setInt(boolean local, String key, int value) {
        (local ? peek() : global).set(key, value);
        return this;
    }

    // Inserts given key value pair
    // Set local to false to bind to global frame
    // Set local to true to bind to top frame
    // Returns the state
    public State setFloat(boolean local, String key, float value) {
        (local ? peek() : global).set(key, value);
        return this;
    }

    // Inserts given key value pair
    // Set local to false to bind to global frame
    // Set local to true to bind to top frame
    // Returns the state
    public State setString(boolean local, String key, String value) {
        (local ? peek() : global).set(key, value);
        return this;
    }

    // Initializes given integer array
    // 1. Allocates space in Memory
    // 2. Inserts key-value pair (array name, base address) into Memory
    //    a. If local is false, inserts into global frame
    //    b. If local is true, inserts into top frame
    // Returns the state
    public State initIntArray(boolean local, String key, int size, Integer defaultValue) {
        int address = mem.initIntArray(size, defaultValue);
        (local ? peek() : global).set(key, address);
        return this;
    }

    // Initializes given float array
    // 1. Allocates space in Memory
    // 2. Inserts key-value pair (array name, base address) into Memory
    //    a. If local is false, inserts into global frame
    //    b. If local is true, inserts into top frame
    // Returns the state
    public State initFloatArray(boolean local, String key, int size, Float defaultValue) {
        int address = mem.initFloatArray(size, defaultValue);
        (local ? peek() : global).set(key, address);
        return this;
    }

    // Returns int stored at given index of array key
    // Throws an exception if no array exists
    public Integer loadIntArray(String key, int index) throws Exception {
        Integer address = getInt(key);
        if (address == null) {
            throw new Exception("Cannot load into nonexistent array");
        }
        return mem.getInt(address, index);
    }

    // Returns float stored at given index of array key
    // Throws an exception if no such array exists
    public Float loadFloatArray(String key, int index) throws Exception {
        Integer address = getInt(key);
        if (address == null) {
            throw new Exception("Cannot load into nonexistent array");
        }
        return mem.getFloat(address, index);
    }

    // Stores given int value at given index of array key
    // Throws an exception if no such array exists
    public State storeIntArray(String key, int index, int value) throws Exception {
        Integer address = getInt(key);
        if (address == null) {
            throw new Exception("Cannot store into nonexistent array");
        } else {
            mem.setInt(address, index, value);
        }
        return this;
    }

    // Stores given float value at given index of array key
    // Throws an exception if no such array exists
    public State storeFloatArray(String key, int index, float value) throws Exception {
        Integer address = getInt(key);
        if (address == null) {
            throw new Exception("Cannot store into nonexistent array");
        } else {
            mem.setFloat(address, index, value);
        }
        return this;
    }

    // Standard Library Functions
    // printi, printf, readi, readf
    // YOU MUST USE THESE IMPLEMENTATIONS
    // IF FOR SOME REASON YOU CHOOSE TO REWRITE THEM
    // YOUR CODE MUST BE FUNCTIONALLY EQUIVALENT

    public void libPrinti(String key) {
        System.out.print("printi: ");
        System.out.println(getInt(key));
    }

    public void libPrintf(String key) {
        System.out.print("printf: ");
        System.out.println(getFloat(key));
    }

    private Scanner in = new Scanner(System.in);

    public int libReadi() {
        System.out.print("readi: ");
        return in.nextInt();
    }

    public float libReadf() {
        System.out.print("readf: ");
        return in.nextFloat();
    }
}
