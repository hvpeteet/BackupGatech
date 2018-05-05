package compiler.analyzer;

import compiler.shared.Symbol;
import compiler.shared.SymbolType;

import java.util.List;

public class TypeAnalyzer {

    private ExtendedASTInfo extendedInfo;
    private Symbol ast;
    private String funcID;
    private int loopDepth;

    public TypeAnalyzer(Symbol ast, boolean debug) throws TypeException {
        this.ast = ast;
        this.extendedInfo = new ExtendedASTInfo(ast, debug);
        // assertWellTyped();

    }

    public TypeAnalyzer(Symbol ast) throws TypeException {
        this(ast, false);
    }

    public void assertWellTyped() throws TypeException {
        assertWellTyped(ast);
    }

    private void assertWellTyped(Symbol symbol) throws TypeException {
        if (symbol.getType() == SymbolType.FUNCDECL) {
            funcID = symbol.getChildren().get(1).getVal();
        }

        SymbolType firstType = (symbol.getChildren().size()) > 0 ? symbol.getChild(0).getType() : null;
        if (firstType == SymbolType.FOR || firstType == SymbolType.WHILE) {
            loopDepth++;
        }
        for (Symbol child : symbol.getChildren()) {
            assertWellTyped(child);
        }
        switch(symbol.getType()) {
            case VARDECL:
                assertWellTypedVARDECL(symbol);
                break;
            case STMT:
                assertWellTypedSTMT(symbol);
                break;
            case PRED:
                assertWellTypedPRED(symbol);
                break;
        }
        for (Symbol child : symbol.getChildren()) {
            assertWellTyped(child);
        }
        if (symbol.getType() == SymbolType.FUNCDECL) {
            funcID = null;
        }
        if (firstType == SymbolType.FOR || firstType == SymbolType.WHILE) {
            loopDepth--;
        }
    }

    private void assertWellTypedVARDECL(Symbol symbol) throws TypeException {
        Symbol optinit = symbol.getChild(4);
        if (optinit.getChildren().size() > 0) {
            TypeObj varType = extendedInfo.getType(symbol.getChild(3));
            TypeObj constType = getType(optinit.getChild(1));
            if (!assignmentTypesCompatible(varType, constType)) {
                throw new TypeException("initializing variable with incompatible type");
            }
        }
    }

    private void assertWellTypedSTMT(Symbol symbol) throws TypeException {
        List<Symbol> children = symbol.getChildren();
        Symbol firstChild = symbol.getChildren().get(0);

        switch(firstChild.getType()) {
            case LVALUE:
                TypeObj lvalueType = getType(firstChild);
                TypeObj numexprType = getType(children.get(2));
                if (!assignmentTypesCompatible(lvalueType, numexprType)) {
                    throw new TypeException("variable assignment has incompatible types");
                }
                break;
            case FOR:
                if (getType(children.get(1)).getType() != Type.INT) {
                    throw new TypeException("non-integer loop variable");
                }
                if ((getType(children.get(3)).getType() != Type.INT) || (getType(children.get(5)).getType() != Type.INT)) {
                    throw new TypeException("non-integer range in for loop");
                }
                break;
            case OPTSTORE:
                assertWellTypedFuncCall(symbol);
                break;
            case BREAK:
                if (loopDepth < 1) {
                    throw new TypeException("illegal break, not inside any loops");
                }
                break;
            case RETURN:
                if (funcID == null) {
                    throw new TypeException("using return statement outside of function");
                } else if (!assignmentTypesCompatible(extendedInfo.getRetType(funcID), getType(children.get(1)))) {
                    throw new TypeException("invalid return type for function " + funcID);
                }
                break;
        }
    }

    private void assertWellTypedFuncCall(Symbol symbol) throws TypeException {
        List<Symbol> children = symbol.getChildren();
        Symbol optstore = children.get(0);
        String calledFuncID = children.get(1).getVal();
        TypeObj funcRetType = extendedInfo.getRetType(calledFuncID);
        if (!optstore.getChildren().isEmpty()) {
            Symbol lvalue = optstore.getChildren().get(0);
            TypeObj lvalueType = getType(lvalue);
            if (!assignmentTypesCompatible(lvalueType, funcRetType)) {
                throw new TypeException("variable assignment has incompatible types");
            }
        }
        int paramIndex = 0;
        Symbol paramsNumexprs = children.get(3);
        if (!paramsNumexprs.getChildren().isEmpty()) {
            Symbol neexprs = paramsNumexprs.getChildren().get(0);
            boolean allParamsChecked = false;
            do {
                Symbol numexpr = neexprs.getChildren().get(0);
                if (!getType(numexpr).equals(extendedInfo.getParamType(calledFuncID, paramIndex))) {
                    throw new TypeException("invalid parameter to function " + calledFuncID);
                }
                paramIndex++;
                if (neexprs.getChildren().size() > 1) {
                    neexprs = neexprs.getChildren().get(2);
                } else {
                    allParamsChecked = true;
                }
            } while (!allParamsChecked);
        }
        if (paramIndex < extendedInfo.getNumParams(calledFuncID)) {
            throw new TypeException("missing function parameters");
        }
    }

    private void assertWellTypedPRED(Symbol symbol) throws TypeException {
        Symbol firstChild = symbol.getChildren().get(0);
        if (firstChild.getType() == SymbolType.NUMEXPR) {
            Type firstExpType = getType(firstChild).getType();
            Type secondExpType = getType(symbol.getChildren().get(2)).getType();
            if (!(firstExpType == Type.INT || firstExpType == Type.FLOAT)) {
                throw new TypeException("non numeric operand to boolean operator");
            }
            if (!(secondExpType == Type.INT || secondExpType == Type.FLOAT)) {
                throw new TypeException("non numeric operand to boolean operator");
            }
        }
    }

    public TypeObj getType(Symbol symbol, String funcID) throws TypeException {
        TypeObj result = null;
        String old_funcid = this.funcID;
        this.funcID = funcID;
        switch(symbol.getType()) {
            case NUMEXPR:
                result = getNUMEXPRType(symbol);
                break;
            case TERM:
                result = getTERMType(symbol);
                break;
            case FACTOR:
                result = getFACTORType(symbol);
                break;
            case CONST:
                result = extendedInfo.getConstType(symbol);
                break;
            case ID:
                result = extendedInfo.getVarType(symbol.getVal(), funcID);
                break;
            case LVALUE:
                result = getLVALUEType(symbol);
                break;
        }
        this.funcID = old_funcid;
        return result;
    }

    public TypeObj getType(Symbol symbol) throws TypeException {
        return getType(symbol, funcID);
    }

    private TypeObj getNUMEXPRType(Symbol symbol) throws TypeException {
        TypeObj result = null;
        Symbol firstChild = symbol.getChildren().get(0);
        if (firstChild.getType() == SymbolType.TERM) {
            result = getType(firstChild);
        } else {
            Symbol termChild = symbol.getChildren().get(2);
            Type termType = getType(termChild).getType();
            Type numexprType = getType(firstChild).getType();
            if (!(termType == Type.FLOAT || termType == Type.INT)) {
                throw new TypeException("non numeric operand to operator");
            }
            if (!(numexprType == Type.FLOAT || numexprType == Type.INT)) {
                throw new TypeException("non numeric operand to operator");
            }
            if (numexprType == Type.FLOAT || termType == Type.FLOAT) {
                result = new TypeObj(Type.FLOAT, 0);
            } else {
                result = new TypeObj(Type.INT, 0);
            }
        }
        return result;
    }

    private TypeObj getTERMType(Symbol symbol) throws TypeException {
        TypeObj result = null;
        Symbol firstChild = symbol.getChildren().get(0);
        if (firstChild.getType() == SymbolType.FACTOR) {
            result = getType(firstChild);
        } else {
            Symbol factorChild = symbol.getChildren().get(2);
            Type factorType = getType(factorChild).getType();
            Type termType = getType(firstChild).getType();
            if (!(factorType == Type.FLOAT || factorType == Type.INT)) {
                throw new TypeException("non numeric operand to operator");
            }
            if (!(termType == Type.FLOAT || termType == Type.INT)) {
                throw new TypeException("non numeric operand to operator");
            }
            if (termType == Type.FLOAT || factorType == Type.FLOAT) {
                result = new TypeObj(Type.FLOAT, 0);
            } else {
                result = new TypeObj(Type.INT, 0);
            }
        }
        return result;
    }

    private TypeObj getFACTORType(Symbol symbol) throws TypeException {
        TypeObj result = null;
        Symbol firstChild = symbol.getChildren().get(0);
        if (symbol.getChildren().size() == 1) {
            result = getType(firstChild);
        } else if (firstChild.getType() == SymbolType.OPEN_PAREN) {
            result = getType(symbol.getChildren().get(1));
        } else {
            TypeObj idType = extendedInfo.getVarType(firstChild.getVal(), funcID);
            if (!(idType.getType() == Type.INT_ARR || idType.getType() == Type.FLOAT_ARR)) {
                throw new TypeException("attempting to index into non-array type");
            }
            TypeObj indexType = getType(symbol.getChildren().get(2));
            if (!(indexType.getType() == Type.INT)) {
                throw new TypeException("array index not an integer");
            }
            result = idType.elementOf();
        }
        return result;
    }

    private TypeObj getLVALUEType(Symbol symbol) throws TypeException {
        TypeObj result = null;
        TypeObj idType = getType(symbol.getChildren().get(0));
        Symbol optoffset = symbol.getChildren().get(1);
        if (optoffset.getChildren().size() > 0) {
            if (getType(optoffset.getChildren().get(1)).getType() != Type.INT) {
                throw new TypeException("array index not an integer");
            } else {
                if (!(idType.getType() == Type.INT_ARR || idType.getType() == Type.FLOAT_ARR)) {
                    throw new TypeException("attempting to index into non-array type");
                }
                result = idType.elementOf();
            }
        } else {
            result = idType;
        }
        return result;
    }

    private boolean assignmentTypesCompatible(TypeObj varType, TypeObj valueType) {
        if (varType == null) {
            return false;
        }
        if (varType.equals(valueType)) {
            return true;
        }
        if (varType.getType() == Type.FLOAT && valueType.getType() == Type.INT) {
            return true;
        }
        return false;
    }
}