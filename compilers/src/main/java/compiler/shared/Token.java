package compiler.shared;

/**
* Token class.
* @author Chris Clegg
*/

public class Token {

    private SymbolType symbolType;
    private String value;

    /**
    * Constructor that takes in the type of this token and the literal string value from the input
    * that matches the symbol type.
    * @param symbolType Type of the token read from input
    * @param value Literal value of the token read from input
    */

    public Token(SymbolType symbolType, String value) {
        this.symbolType = symbolType;
        this.value = value;
    }

    public SymbolType getSymbolType() {
        return symbolType;
    }

    public void setSymbolType(SymbolType symbolType) {
        this.symbolType = symbolType;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String toString() {
        String result = symbolType.toString();
        if ((symbolType == SymbolType.ID) || (symbolType == SymbolType.INT_LIT) || (symbolType == SymbolType.FLOAT_LIT)) {
            result = value + ":" + symbolType;
        }
        return result;
    }
}