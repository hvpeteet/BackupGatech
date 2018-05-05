package compiler.parser;

import compiler.shared.Symbol;
import compiler.shared.SymbolType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;


public class ParseItem {

    private SymbolType left;
    private SymbolType[] right;
    private int placeholder;
    private SymbolType next;

    /**
     * Represents a parse item (shocker...)
     * A parse item is a rule that has a placeholder such as
     * (A --> B * C, EOF/symbol)
     *         ^------ placeholder
     * @param left the left hand side of the rule
     * @param right the right hand side of the rule
     * @param placeholder the index of the placeholder to be inserted at.
     * @aaram next the next symbol that can be accepted;
    */
    public ParseItem(SymbolType left, SymbolType[] right, int placeholder, SymbolType next) {
        this.left = left;
        this.right = right;
        this.placeholder = placeholder;
        this.next = next;
        if (next == SymbolType.EPSILON) {
            throw new RuntimeException("epsilon given as next, next must be non-epsilon terminal");
        }
        while (needsNext() == SymbolType.EPSILON) {
            if (placeholder > right.length) {
                throw new RuntimeException("parse item ended with epsilon left" + left + " right " + right + "next " + next);
            }
            this.placeholder++;
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("(");
        builder.append(left.toString());
        builder.append(" --> ");
        int i = 0;
        for (; i < placeholder && i < right.length; i++) {
            builder.append(right[i].toString());
            builder.append(" ");
        }
        builder.append("*");
        for (; i < right.length; i++) {
            builder.append(" ");
            builder.append(right[i].toString());
        }
        builder.append(", ");
        builder.append(next.toString());
        builder.append(")");
        return builder.toString();
    }

    public int hashCode() {
        return this.toString().hashCode();
    }

    @Override
    public boolean equals(Object otherObj) {
        if (otherObj == null || !(otherObj instanceof ParseItem)) {
            return false;
        }
        ParseItem other = (ParseItem) otherObj;
        if (!(this.left == other.left)) {
            return false;
        }
        if (this.right.length != other.right.length) {
            return false;
        }
        if (this.next != other.next) {
            return false;
        }
        if (this.next != other.next) {
            return false;
        }
        if (this.placeholder != other.placeholder) {
            return false;
        }
        for (int i = 0; i < this.right.length; i++) {
            if (!this.right[i].equals(other.right[i])) {
                return false;
            }
        }
        return true;
    }

    /**
     * If the parse item has been completed.
     * @return if the parse item is accepting (can be reduced)
    */
    public boolean isComplete(Symbol next) {
        return placeholder == right.length && next.getType().equals(this.next);
    }

    /**
     * The successor of this item on the given input
     * @param consumedSymbol the input symbol to apply.
     * @return the successor of this state A --> B * C would return A --> B C *, returns null if not valid.
    */
    public ParseItem getSuccessor(Symbol consumedSymbol) {
        if (placeholder == right.length) {
            return null;
        }
        if (!consumedSymbol.getType().equals(right[placeholder])) {
            return null;
        } else {
            return new ParseItem(left, right, placeholder + 1, next);
        }
    }

    /**
     * Gets the symbol type that is needed next in completing this item.
     * @return the symbol type that is needed next in completing the item.
    */
    public SymbolType needsNext() {
        if (placeholder == right.length) {
            return next;
        }
        return right[placeholder];
    }
    /**
     * Computes the firsts of the rest of a string, required for closure.
     * @return the extended set of firsts.
    */
    public Set<SymbolType> closureExtendedFirsts() {
        Set<SymbolType> firsts = new HashSet<>();
        if (placeholder + 1 == right.length) {
            firsts.add(next);
            return firsts;
        }
        firsts.addAll(right[placeholder + 1].firsts());
        int index = placeholder + 2;
        while (firsts.contains(SymbolType.EPSILON) && index < right.length) {
            firsts.remove(SymbolType.EPSILON);
            firsts.addAll(right[index].firsts());
            index++;
        }
        if (firsts.contains(SymbolType.EPSILON)) {
            firsts.remove(SymbolType.EPSILON);
            firsts.add(next);
        }
        return firsts;
    }

    public SymbolType[] getRight() {
        return this.right;
    }

    public SymbolType getLeft() {
        return this.left;
    }
}