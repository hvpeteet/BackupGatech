package compiler.shared;

/**
* Interface for Scanner module. 
* @author Chris Clegg
*/

public interface Scanner {

    /**
    * Returns the next Token read from the input to the Scanner without removing it from the input buffer.
    * @return nextToken
    */
    public Token peek();

    /**
    * Returns the next Token read from the input to the Scanner and removes it from the input buffer.
    * @return nextToken
    */
    public Token pop();

}