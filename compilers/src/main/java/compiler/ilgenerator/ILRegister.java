package compiler.ilgenerator;
import compiler.analyzer.TypeObj;

public class ILRegister {
    private TypeObj type;
    private String name;

    public ILRegister(String name, TypeObj type) {
        this.type = type;
        this.name = name;
    }

    public String toString() {
        return this.name;
    }

    public String getName() {
        return this.name;
    }

    public TypeObj getType() {
        return this.type;
    }
}