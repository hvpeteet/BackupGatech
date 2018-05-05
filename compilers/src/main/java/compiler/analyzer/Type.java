package compiler.analyzer;

public enum Type {
    INT,
    FLOAT,
    BOOLEAN,
    INT_ARR,
    FLOAT_ARR;

    private Type elementType;
    private Type arrType;

    public Type getElementType() {
        return this.elementType;
    }

    public Type toArrType() {
        return this.arrType;
    }

    static {
        INT.arrType = INT_ARR;
        FLOAT.arrType = FLOAT_ARR;

        INT_ARR.elementType = INT;
        FLOAT_ARR.elementType = FLOAT;
    }

}