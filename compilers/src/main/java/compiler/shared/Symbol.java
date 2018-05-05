package compiler.shared;

import java.util.ArrayList;
import java.util.List;

public class Symbol {
    private String val;
    private List<Symbol> children;
    private SymbolType type;

    /**
     * Constructs a new symbol.
     * @param val The original value from the string being parsed.
     * @param children The children of this symbol in the parse tree.
     * @param type The type of the symbol.
    */
    public Symbol(String val, List<Symbol> children, SymbolType type) {
        this.val = val;
        if (children == null) {
            this.children = new ArrayList<Symbol>();
        } else {
            this.children = children;
        }
        this.type = type;
    }

    /**
     * Constructs a symbol from a token.
     * @param token the token to create the symbol from.
    */
    public Symbol(Token token) {
        this.val = token.getValue();
        this.children = new ArrayList<>();
        this.type = token.getSymbolType();
    }

    public String getVal() {
        return this.val;
    }

    public SymbolType getType() {
        return this.type;
    }

    public void addChild(Symbol symbol) {
        this.children.add(symbol);
    }

    /**
     * Gets the children of this node.
     * @return the children of this node in the correct order.
    */
    public List<Symbol> getChildren() {
        List<Symbol> toReturn = new ArrayList<>();
        for (int i = 0; i < children.size(); i++) {
            toReturn.add(children.get(i));
        }
        return toReturn;
    }

    public Symbol getChild(int index) {
        return this.children.get(index);
    }

    /**
     * Adjust ast to grammar.
     * @return the children of the "fixed" symbol with the fix pre-pended.
    */
    public List<Symbol> fix() {
        if (this.type.getFix() != null) {
            switch(this.type) {
                case OPTSTORE_FIX_0:
                    children.add(0, new Symbol("", null, SymbolType.OPTSTORE));
                    break;
                case OPTSTORE_FIX_1:
                    Symbol newChild = new Symbol("", null, SymbolType.OPTSTORE);
                    newChild.addChild(children.remove(0));
                    newChild.addChild(children.remove(0));
                    this.children.add(0, newChild);
                    break;
                default:
                    System.err.println("unsupported fix, malformed language file");
            }
        }
        List<Symbol> newChildren = new ArrayList<>();
        for (Symbol child : children) {
            newChildren.addAll(child.fix());
        }
        children = newChildren;
        List<Symbol> self = new ArrayList<>();
        self.add(this);
        if (this.type.getFix() == null) {
            return self;
        } else {
            return children;
        }
    }

    /**
     * A string representation of this symbol with its children.
     * This is a representation of the AST.
    */
    public String toString() {
        if (type.isTerm()) {
            return val;
        } else if (children.size() == 0) {
            return type.toString(); // replace with special format for tests
        } else {
            StringBuilder rep = new StringBuilder();
            rep.append("(");
            rep.append(type.toString()); // replace with special format for tests
            for (Symbol s : children) {
                rep.append(" ");
                rep.append(s.toString());
            }
            rep.append(")");
            return rep.toString();
        }
    }
}