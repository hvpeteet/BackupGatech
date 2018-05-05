package compiler.analyzer;

import compiler.shared.Symbol;
import compiler.shared.SymbolType;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;

public class ExtendedASTInfo {

    private Symbol ast;
    // Variable types

    private Map<String, TypeObj> type_aliases;

    public Map<String, TypeObj> global_var_types;                 // var id --> type
    public Map<String, Map<String, TypeObj>> local_var_types;     // function id --> var id --> type
    // Function types
    private Map<String, List<TypeObj>> param_types;
    public Map<String, TypeObj> ret_types;

    /**
     * Creates a new type analyzer for the given AST
     * @param ast the Abstract Syntax Tree to be analyzed.
    */
    public ExtendedASTInfo(Symbol ast, boolean debug) throws TypeException {
        this.ast = ast;

        initializeTypeAliases();
        if (debug) {
            System.out.println("Type aliases:");
            for (String alias : type_aliases.keySet()) {
                System.out.println(alias + "-->" + type_aliases.get(alias));
            }
        }

        initializeGlobalVarTypes();
        if (debug) {
            System.out.println("Global var types:");
            for (String name : global_var_types.keySet()) {
                System.out.println(name + "-->" + global_var_types.get(name));
            }
        }

        initializeFunctionTypes();
        if (debug) {
            System.out.println("Local var types:");
            for (String function : local_var_types.keySet()) {
                System.out.print(function + "(");
                for (String param : local_var_types.get(function).keySet()) {
                    System.out.print(param + " : " + local_var_types.get(function).get(param) + ", ");
                }
                System.out.println(")");
            }
            System.out.println("Param Types:");
            for (String function : param_types.keySet()) {
                System.out.print(function + "(");
                for (TypeObj param : param_types.get(function)) {
                    System.out.print(param + ", ");
                }
                System.out.println(")");
            }
            System.out.println("Return types:");
            for (String function : ret_types.keySet()) {
                System.out.println(function + "-->" + ret_types.get(function));
            }
        }
    }

    public ExtendedASTInfo(Symbol ast) throws TypeException {
        this(ast, false);
    }

    /**
     * Gets the type of a constant.
     * @param constant the constant to get the type of
     * @return the type of the constant.
    */
    public TypeObj getConstType(Symbol constant) {
        if (constant.getType() != SymbolType.CONST) {
            throw new RuntimeException("getConstType called on non const symbol:\n" + constant.toString());
        }
        if (constant.getChild(0).getType() == SymbolType.FLOAT_LIT) {
            return new TypeObj(Type.FLOAT, 0);
        } else {
            return new TypeObj(Type.INT, 0);
        }
    }

    public int getNumParams(String functionName) {
        return param_types.get(functionName).size();
    }

    /**
     * Get the type of a variable.
     * @param id the name of the variable.
     * @param function the name of a function whose scope you want to include (set to null if you are just in global scope)
     * @throws TypeException if the variable is not declared.
     * @return the type of the variable.
    */
    public TypeObj getVarType(String id, String function) throws TypeException {
        TypeObj toReturn = null;
        // If in function check locals
        if (local_var_types.get(function) != null) {
            toReturn = local_var_types.get(function).get(id);
        }
        // Check globals
        if (toReturn == null) {
            toReturn = global_var_types.get(id);
        }
        // No matches found: throw a type exception
        if (toReturn == null) {
            throw new TypeException("variable \"" + id + "\" is used but not declared");
        }
        return toReturn;
    }

    public TypeObj getVarType(Symbol var, String function) throws TypeException {
        if (var.getType() != SymbolType.VAR) {
            throw new RuntimeException("non var symbol passed into getVarType");
        }
        return this.getVarType(var.getVal(), function);
    }

    /**
     * Gets the return type of a function.
     * @param id the function name.
     * @throws TypeException if the function does not exist.
     * @return the type that the function returns. NOTE this can be null if the function is void
    */
    public TypeObj getRetType(String functionName) throws TypeException {
        if (!ret_types.containsKey(functionName)) {
            throw new TypeException("function \"" + functionName + "\" is used but not declared");
        }
        return ret_types.get(functionName);
    }

    /**
     * Gets the type of a parameter of a function.
     * @param func_id the function name.
     * @param param_index the index of the parameter
     * @throws TypeException if the function does not exist or the param_index is out of bounds.
     * @return the type of the parameter.
    */
    public TypeObj getParamType(String func_id, int param_index) throws TypeException {
        if (!param_types.containsKey(func_id)) {
            throw new TypeException("function \"" + func_id + "\" is used but not declared");
        }
        List<TypeObj> params = param_types.get(func_id);
        if (params.size() <= param_index) {
            throw new TypeException("function \"" + func_id + "\" has too many arguments");
        }
        return params.get(param_index);
    }

    public TypeObj getOpType(TypeObj left, Symbol op, TypeObj right) throws TypeException {
        if (left.getType() == Type.INT_ARR || left.getType() == Type.FLOAT_ARR || right.getType() == Type.INT_ARR || right.getType() == Type.FLOAT_ARR) {
            throw new TypeException("array type argument to arithmetic opertion");
        }
        if (left.getType() == Type.FLOAT || right.getType() == Type.FLOAT) {
            return new TypeObj(Type.FLOAT, 0);
        } else {
            return new TypeObj(Type.INT, 0);
        }
    }

// ---------------------------INTERNALS(gross)---------------------

    private TypeObj getArrayType(Symbol arrType) throws TypeException {
        Symbol singleType = arrType.getChild(5);
        int arr_size = Integer.parseInt(arrType.getChild(2).getVal());
        if (singleType.getChildren().size() > 1) {
            return getArrayType(singleType).arrayOf(arr_size);
        }
        Symbol base = singleType;
        return getType(base).arrayOf(arr_size);
    }

    private void initializeTypeAliases() throws TypeException {
        type_aliases = new HashMap<>();
        type_aliases.put("float", new TypeObj(Type.FLOAT, 0));
        type_aliases.put("int", new TypeObj(Type.INT, 0));

        Symbol type_decls = ast.getChildren().get(1).getChildren().get(0);
        Set<Symbol> alias_decls = new HashSet<>();
        Symbol typedecls = type_decls;
        while (typedecls.getChildren().size() > 0) {
            alias_decls.add(typedecls.getChildren().get(0));
            typedecls = typedecls.getChildren().get(1);
        }
        while (alias_decls.size() > 0) {
            Set<Symbol> toRemove = new HashSet<>();
            for (Symbol alias_decl : alias_decls) {
                String alias = alias_decl.getChildren().get(1).getVal();
                if (type_aliases.get(alias) != null) {
                    throw new TypeException("Same alias used twice " + alias);
                }
                Symbol base_symbol = alias_decl.getChildren().get(3);
                if (base_symbol.getChildren().size() != 1) {
                    // Is an array
                    type_aliases.put(alias, getArrayType(base_symbol));
                    toRemove.add(alias_decl);
                } else {
                    // Is not an array
                    String base = base_symbol.getChildren().get(0).getVal();
                    if (type_aliases.get(base) != null) {
                        type_aliases.put(alias, type_aliases.get(base));
                        toRemove.add(alias_decl);
                    }
                }
            }
            int oldSize = alias_decls.size();
            alias_decls.removeAll(toRemove);
            int newSize = alias_decls.size();
            if (oldSize == newSize) {
                throw new TypeException("Unresolvable type declarations " + alias_decls);
            }
        }
    }

    public TypeObj getType(Symbol aType) throws TypeException {
        if (aType.getType() != SymbolType.VAR_TYPE) {
            throw new RuntimeException("Must pass in a symbol of type SymbolType.VAR_TYPE");
        }
        if (aType.getChildren().size() > 1) {
            return getArrayType(aType);
        } else {
            String alias = aType.getChild(0).getVal();
            TypeObj dat_type = type_aliases.get(alias);
            if (dat_type == null) {
                throw new TypeException("Type \"" + alias + "\" used but not declared ");
            } else {
                return dat_type;
            }
        }
    }

    private void initializeGlobalVarTypes() throws TypeException {
        global_var_types = new HashMap<>();
        Symbol vardecls = ast.getChild(1).getChild(1);

        // Get a list of all the declarations
        List<Symbol> decls = new ArrayList<>();
        while(vardecls.getChildren().size() > 0) {
            decls.add(vardecls.getChild(0));
            vardecls = vardecls.getChild(1);
        }

        for (Symbol decl : decls) {
            // System.out.println("working on decl " + decl);
            Symbol type_symbol = decl.getChild(3);
            TypeObj real_type = getType(type_symbol);
            List<String> var_names = new ArrayList<>();
            Symbol ids = decl.getChild(1);
            while(ids.getChildren().size() > 1) {
                //System.out.println("ids = " + ids);
                var_names.add(ids.getChild(0).getVal());
                ids = ids.getChild(2);
            }
            var_names.add(ids.getChild(0).getVal());
            //System.out.println("var names = " + var_names);
            for (String name : var_names) {
                if (global_var_types.get(name) != null) {
                    throw new TypeException("the variable name \"" + name + "\" is used twice");
                } else {
                    global_var_types.put(name, real_type);
                }
            }
        }
     }

    private void initializeFunctionTypes() throws TypeException {
        param_types = new HashMap<>();
        ret_types = new HashMap<>();
        local_var_types = new HashMap<>();

        ret_types.put("printi", null);
        List<TypeObj> printi_args = new ArrayList<>();
        printi_args.add(new TypeObj(Type.INT, 0));
        param_types.put("printi", printi_args);

        ret_types.put("printf", null);
        List<TypeObj> printf_args = new ArrayList<>();
        printf_args.add(new TypeObj(Type.FLOAT, 0));
        param_types.put("printf", printf_args);

        ret_types.put("readi", new TypeObj(Type.INT, 0));
        List<TypeObj> readi_args = new ArrayList<>();
        param_types.put("readi", readi_args);

        ret_types.put("readf", new TypeObj(Type.FLOAT, 0));
        List<TypeObj> readf_args = new ArrayList<>();
        param_types.put("readf", readf_args);


        Set<Symbol> uninitialized_funcdecls = new HashSet<>();
        // get the initial funcdecls node
        Symbol next = ast.getChild(1).getChild(2);
        while (next.getChildren().size() > 0) {
            uninitialized_funcdecls.add(next.getChild(0));
            next = next.getChild(1);
        }

        for (Symbol to_initialize : uninitialized_funcdecls) {
            String func_name = to_initialize.getChild(1).getVal();
            if (ret_types.get(func_name) != null) {
                throw new TypeException("The function name \"" + func_name + "\" is used twice");
            }
            //System.out.println("in function " + func_name);
            local_var_types.put(func_name, new HashMap<String, TypeObj>());
            // Set return type
            Symbol opt_ret_type = to_initialize.getChild(5);
            if (opt_ret_type.getChildren().size() > 0) {
                //System.out.println("getting return type, opt_ret_type = " + opt_ret_type.getChildren());
                TypeObj derived_type = getType(opt_ret_type.getChild(1));
                ret_types.put(func_name, derived_type);
            } else {
                ret_types.put(func_name, null);
            }
            // Set param types
            Symbol params_node = to_initialize.getChild(3);
            if (params_node.getChildren().size() == 0) {
                // has no params
                param_types.put(func_name, new ArrayList<TypeObj>());
            } else {
                List<Symbol> params = new ArrayList<>();
                Symbol neparams = params_node.getChild(0);
                // Get normalized list of params
                while (neparams.getChildren().size() > 1) {
                    params.add(neparams.getChild(0));
                    neparams = neparams.getChild(2);
                }
                params.add(neparams.getChild(0));
                //System.out.println("params = " + params);
                // Get the types
                List<TypeObj> types = new ArrayList<>();
                Map<String, TypeObj> named_types = local_var_types.get(func_name);
                for (Symbol param : params) {
                    TypeObj param_type = getType(param.getChild(2));
                    String param_name = param.getChild(0).getVal();
                    types.add(param_type);
                    // if (global_var_types.get(param_name) != null) {
                    //     throw new TypeException("param name \"" + param_name + "\" matches global variable name (would loose local access to this global var)");
                    // }
                    if (named_types.get(param_name) != null) {
                        throw new TypeException("two params have the same name \"" + param_name + "\"");
                    }
                    named_types.put(param_name, param_type);
                }
                param_types.put(func_name, types);
            }
        }
    }
}