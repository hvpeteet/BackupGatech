package compiler.analyzer;

import java.util.List;
import java.util.ArrayList;

public class TypeObj {
    private Type type;
    private int depth;
    private List<Integer> layer_sizes;

    public TypeObj(Type type, int depth) {
        this.type = type;
        this.depth = depth;
        this.layer_sizes = new ArrayList<>();
    }

    public TypeObj(Type type, int depth, List<Integer> layer_sizes) {
        this(type, depth);
        this.layer_sizes = layer_sizes;
    }

    public Type getType() {
        return this.type;
    }

    public int getDepth() {
        return this.depth;
    }

    public int getTopLayer() {
        return this.layer_sizes.get(0);
    }

    public TypeObj arrayOf(int arrLen) {
        List<Integer> layer_sizes_cpy = new ArrayList<>();
        for (int i : this.layer_sizes) {
            layer_sizes_cpy.add(i);
        }
        layer_sizes_cpy.add(0, arrLen);
        // If this is not an arry type already
        if (this.type.toArrType() != null) {
            return new TypeObj(this.type.toArrType(), 1, layer_sizes_cpy);
        }
        // Was already an array type
        return new TypeObj(this.type, this.depth + 1, layer_sizes_cpy);
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof TypeObj) {
            return this.type == ((TypeObj) other).type && this.depth == ((TypeObj) other).depth;
        } else {
            return false;
        }
    }

    public String toString() {
        return "(type=" + this.type.toString() + ", depth=" + this.depth + ", layers=" + this.layer_sizes + ")";
    }

    public TypeObj elementOf() {
        if (this.type.getElementType() == null) {
            throw new RuntimeException("Cannot get element of a non array type (in Type obj)");
        }
        if (this.depth - 1 == 0) {
            return new TypeObj(this.type.getElementType(), 0);
        }
        List<Integer> layer_sizes_cpy = new ArrayList<>();
        for (int i : this.layer_sizes) {
            layer_sizes_cpy.add(i);
        }
        layer_sizes_cpy.remove(0);
        return new TypeObj(this.type, this.depth - 1, layer_sizes_cpy);
    }
}