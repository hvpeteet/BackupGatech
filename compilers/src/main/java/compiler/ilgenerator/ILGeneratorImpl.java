package compiler.ilgenerator;

import java.util.List;
import java.util.ArrayList;
import compiler.shared.Symbol;
import compiler.shared.ILGenerator;
import compiler.analyzer.ExtendedASTInfo;
import java.util.Map;
import java.util.HashMap;
import compiler.analyzer.TypeException;
import compiler.analyzer.TypeObj;
import compiler.analyzer.Type;
import compiler.analyzer.TypeAnalyzer;
import java.util.Stack;
import compiler.shared.SymbolType;

/**
 * This class holds all the necessary information to generate the IL code from one AST.
 * NOTE: DO NOT USE THIS FOR MULTIPLE ASTs
 * Normal usage is:
 * ILGenerator my_gen = new ILGeneratorImpl(ast);
 * List<ILInstruction> program = my_gen.generate();
 * // use program here
*/
public class ILGeneratorImpl implements ILGenerator {
    private Map<String, ILRegister> global_variable_regs;
    private Map<String, Map<String, ILRegister>> param_regs;
    private int next_regno;
    private String current_function;
    private Symbol AST;
    private List<ILInstruction> instructions;
    private ExtendedASTInfo extended_info;
    private Symbol ast;
    private TypeAnalyzer analyzer;
    private int ifCount;
    private int loopCount;
    private Stack<Integer> loop_stack;
    private int or_num;
    private int and_num;
    private int ialloc_num;
    private int falloc_num;
    private List<Symbol> param_symbols;

    public static final String ENTRY_POINT = "ENTRY:POINT:MAIN:CODE";

    private static int MEM_SIZE = 1000;

    /**
     * Creates a new ILGeneratorImpl.
     * @throws TypeException if the AST is not well typed
    */
    public ILGeneratorImpl(Symbol AST) throws TypeException {
        this.extended_info = new ExtendedASTInfo(AST);
        this.analyzer = new TypeAnalyzer(AST, false);
        this.analyzer.assertWellTyped();
        this.ast = AST;
        this.loop_stack = new Stack<>();
        this.instructions = new ArrayList<>();
        this.global_variable_regs = new HashMap<>();
        this.param_regs = new HashMap<>();
        for (Map.Entry<String, TypeObj> global : this.extended_info.global_var_types.entrySet()) {
            String varname = global.getKey();
            TypeObj t = global.getValue();
            global_variable_regs.put(varname, allocReg(t));
        }
        for (Map.Entry<String, Map<String, TypeObj>> func : this.extended_info.local_var_types.entrySet()) {
            Map<String, ILRegister> func_map = new HashMap<>();
            for (Map.Entry<String, TypeObj> param : func.getValue().entrySet()) {
                String varname = param.getKey();
                TypeObj t = param.getValue();
                func_map.put(varname, allocReg(t));
            }
            param_regs.put(func.getKey(), func_map);
        }
    }

    /*
     * Creates a list of instructions from the AST.
    */
    public List<ILInstruction> generate() {
        try {
            recursiveGenerate(this.ast, null);
        } catch (TypeException e) {
            System.err.println("Type error in AST given to generate:" + e.getMessage());
            return null;
        }
        return this.instructions;
    }

    public String funcnameToLabelName(String funcName) {
        return "func:" + funcName + ":start";
    }

    /**
     * Helper method for generate, just allows for recursive calls.
     * Emits needed instructions and calls recursively.
     * @param AST the root of the AST you want to work on, this can be a subtree
     * @param scope the function that you are currently inside.
    */
    public ILRegister recursiveGenerate(Symbol AST, String scope) throws TypeException {
        ILRegister to_return = null;
        switch(AST.getType()) {
            case INT_LIT:
                to_return = allocReg(new TypeObj(Type.INT, 0));
                ILInstruction ldi = new ILInstruction(InstructionType.LDI, to_return, Integer.parseInt(AST.getVal()));
                emit(ldi);
                break;
            case FLOAT_LIT:
                to_return = allocReg(new TypeObj(Type.FLOAT, 0));
                ILInstruction ldf = new ILInstruction(InstructionType.LDF, to_return, Float.parseFloat(AST.getVal()));
                emit(ldf);
                break;
            case TYPEDECL:
                // do nothing
                break;
            case VARDECL:
                // Get a list of all ids (for multiple instantiation)
                List<Symbol> ids = new ArrayList<>();
                Symbol root_ids_node = AST.getChild(1);
                while(root_ids_node.getChildren().size() > 1) {
                    ids.add(root_ids_node.getChild(0));
                    root_ids_node = root_ids_node.getChild(2);
                }
                ids.add(root_ids_node.getChild(0));

                // if initialized
                if (AST.getChild(4).getChildren().size() > 0) {
                    ILRegister const_reg = recursiveGenerate(AST.getChild(4).getChild(1).getChild(0), null);
                    for (Symbol id_symbol : ids) {
                        ILRegister var_reg = getVarRegister(id_symbol.getVal(), null);
                        ILInstruction mov = new ILInstruction(InstructionType.MOV, var_reg, const_reg);
                        emit(mov);
                    }
                }
                // if arrays
                for (Symbol id_symbol : ids) {
                    ILRegister arr_reg = getVarRegister(id_symbol.getVal(), null);
                    TypeObj var_type = arr_reg.getType();
                    if (var_type.getType() == Type.INT_ARR || var_type.getType() == Type.FLOAT_ARR) {
                        // System.out.println("Array \"" + id_symbol.getVal() + "\" of type " + var_type);
                        int arr_ptr = alloc_arr(var_type);
                        ILInstruction init_arr_ptr = new ILInstruction(InstructionType.LDI, arr_reg, arr_ptr);
                        emit(init_arr_ptr);
                    }
                }
                break;
            case FUNCDECL:
                ILInstruction start_func_label = new ILInstruction(InstructionType.LABEL, funcnameToLabelName(AST.getChild(1).getVal()));
                emit(start_func_label);
                current_function = AST.getChild(1).getVal();
                Symbol root_params = AST.getChild(3);
                param_symbols = new ArrayList<>();
                if (root_params.getChildren().size() > 0) {
                    Symbol root_neparams = root_params.getChild(0);
                    while (root_neparams.getChildren().size() > 1) {
                        param_symbols.add(root_neparams.getChild(0));
                        root_neparams = root_neparams.getChild(2);
                    }
                    param_symbols.add(root_neparams.getChild(0));
                }
                for (Symbol param : param_symbols) {
                    ILRegister param_reg = getVarRegister(param.getChild(0).getVal(), current_function);
                    ILInstruction save_old_reg = new ILInstruction(InstructionType.PUT_LOCAL, param_reg);
                    emit(save_old_reg);
                }
                for (Symbol param : param_symbols) {
                    ILRegister param_reg = getVarRegister(param.getChild(0).getVal(), current_function);
                    ILInstruction pop_arg = new ILInstruction(InstructionType.POP_ARG, param_reg);
                    emit(pop_arg);
                }

                // evaluate the inside of the function
                // COMMENT THIS BACK IN WHEN POSSIBLE
                recursiveGenerate(AST.getChild(7), current_function);
                // add a return just in case it didn't have a return statement (void function)
                for (Symbol param : param_symbols) {
                    ILRegister param_reg = getVarRegister(param.getChild(0).getVal(), current_function);
                    ILInstruction restore_old_reg = new ILInstruction(InstructionType.POP_LOCAL, param_reg);
                    emit(restore_old_reg);
                }
                ILInstruction func_ret = new ILInstruction(InstructionType.RET);
                emit(func_ret);
                current_function = null;
                break;
            case PROGRAM:
                recursiveGenerate(AST.getChild(1), current_function);
                ILInstruction entry_point = new ILInstruction(InstructionType.LABEL, ENTRY_POINT);
                emit(entry_point);
                recursiveGenerate(AST.getChild(3), current_function);
                break;
            case DECLSEG:
                recursiveGenerate(AST.getChild(0), current_function);
                recursiveGenerate(AST.getChild(1), current_function);
                ILInstruction goto_entry = new ILInstruction(InstructionType.GOTO, ENTRY_POINT);
                emit(goto_entry);
                recursiveGenerate(AST.getChild(2), current_function);
                break;
            case STMT:
                //System.out.println("In statement: " + AST);
                switch(AST.getChild(0).getType()) {
                    case OPTSTORE:
                        // push args
                        List<Symbol> arg_expressions = new ArrayList<>();
                        if (AST.getChild(3).getChildren().size() > 0) {
                            Symbol neexprs = AST.getChild(3).getChild(0);
                            while (neexprs.getChildren().size() > 1) {
                                arg_expressions.add(neexprs.getChild(0));
                                neexprs = neexprs.getChild(2);
                            }
                            arg_expressions.add(neexprs.getChild(0));
                        }

                        List<ILRegister> args = new ArrayList<>();
                        for (Symbol s : arg_expressions) {
                            args.add(recursiveGenerate(s, this.current_function));
                        }
                        for (ILRegister arg : args) {
                            ILInstruction put_arg = new ILInstruction(InstructionType.PUT_ARG, arg);
                            emit(put_arg);
                        }
                        // call
                        ILInstruction call = new ILInstruction(InstructionType.CALL, funcnameToLabelName(AST.getChild(1).getVal()));
                        emit(call);
                        // pop return value
                        if (AST.getChild(0).getChildren().size() > 1) {
                            ILRegister dst = allocReg(extended_info.ret_types.get(AST.getChild(1).getVal()));
                            ILInstruction get_return_val = new ILInstruction(InstructionType.POP_RET_VAL, dst);
                            emit(get_return_val);
                            // TODO(hvpeteet): support assignment in special func assign_to_lvalue
                            assign_to_lvalue(AST.getChild(0).getChild(0), dst);
                        }
                        break;
                    case LVALUE:
                        ILRegister rvalue = recursiveGenerate(AST.getChild(2), current_function);
                        assign_to_lvalue(AST.getChild(0), rvalue);
                        break;
                    case IF:
                        int local_if_count = ifCount;
                        ifCount++;
                        ILInstruction cond_met = new ILInstruction(InstructionType.LABEL, ifCondMetLabelName(local_if_count));
                        ILInstruction cond_not_met = new ILInstruction(InstructionType.LABEL, ifCondNotMetLabelName(local_if_count));
                        ILInstruction done = new ILInstruction(InstructionType.LABEL, ifDoneLabelName(local_if_count));
                        Symbol boolexpr = AST.getChild(1);
                        constructBoolJump(boolexpr, cond_met, cond_not_met);
                        Symbol cond_met_stmts = AST.getChild(3);
                        emit(cond_met);
                        recursiveGenerate(cond_met_stmts, current_function);
                        ILInstruction goto_done = new ILInstruction(InstructionType.GOTO, ifDoneLabelName(local_if_count));
                        emit(goto_done);
                        emit(cond_not_met);
                        if (AST.getChildren().size() > 5) {
                            // else block
                            recursiveGenerate(AST.getChild(5), current_function);
                        }
                        emit(done);
                        break;
                    case FOR:
                        int local_loop_count = loopCount;
                        loopCount++;
                        loop_stack.push(local_loop_count);
                        ILInstruction for_start = new ILInstruction(InstructionType.LABEL, loopStartLabel(local_loop_count));
                        ILInstruction for_done = new ILInstruction(InstructionType.LABEL, loopDoneLabel(local_loop_count));
                        // Create counter register
                        ILRegister counter = getVarRegister(AST.getChild(1).getVal(), current_function);
                        ILRegister counter_init_temp_reg = recursiveGenerate(AST.getChild(3), current_function);
                        ILInstruction init_counter = new ILInstruction(InstructionType.MOV, counter, counter_init_temp_reg);
                        emit(init_counter);
                        ILRegister bound_reg = recursiveGenerate(AST.getChild(5), current_function);

                        emit(for_start);
                        ILInstruction for_loop_run_cond = new ILInstruction(InstructionType.BREQ, counter, bound_reg, loopDoneLabel(local_loop_count));
                        emit(for_loop_run_cond);
                        // body
                        recursiveGenerate(AST.getChild(7), current_function);
                        ILRegister one = allocReg(new TypeObj(Type.INT, 0));
                        emit(new ILInstruction(InstructionType.LDI, one, 1));
                        emit(new ILInstruction(InstructionType.ADD, counter, counter, one));
                        emit(new ILInstruction(InstructionType.GOTO, loopStartLabel(local_loop_count)));
                        // done
                        emit(for_done);
                        loop_stack.pop();
                        break;
                    case WHILE:
                        local_loop_count = loopCount;
                        loopCount++;
                        loop_stack.push(local_loop_count);
                        ILInstruction while_start = new ILInstruction(InstructionType.LABEL, loopStartLabel(local_loop_count));
                        ILInstruction while_cond_met = new ILInstruction(InstructionType.LABEL, loopRunLabel(local_loop_count));
                        ILInstruction while_done = new ILInstruction(InstructionType.LABEL, loopDoneLabel(local_loop_count));

                        emit(while_start);
                        constructBoolJump(AST.getChild(1), while_cond_met, while_done);
                        // body
                        emit(while_cond_met);
                        recursiveGenerate(AST.getChild(3), current_function);
                        emit(new ILInstruction(InstructionType.GOTO, loopStartLabel(local_loop_count)));
                        // done
                        emit(while_done);
                        loop_stack.pop();
                        break;
                    case BREAK:
                        int loop_to_break = loop_stack.peek();
                        String loop_done_label = loopDoneLabel(loop_to_break);
                        ILInstruction goto_loop_done = new ILInstruction(InstructionType.GOTO, loop_done_label);
                        emit(goto_loop_done);
                        break;
                    case RETURN:
                        ILRegister val_reg = recursiveGenerate(AST.getChild(1), current_function);
                        ILInstruction save_ret_val = new ILInstruction(InstructionType.SAVE_RET_VAL, val_reg);
                        emit(save_ret_val);
                        for (Symbol param : param_symbols) {
                            ILRegister param_reg = getVarRegister(param.getChild(0).getVal(), current_function);
                            ILInstruction restore_old_reg = new ILInstruction(InstructionType.POP_LOCAL, param_reg);
                            emit(restore_old_reg);
                        }
                        ILInstruction ret = new ILInstruction(InstructionType.RET);
                        emit(ret);
                        break;
                    }
                break;
            case NUMEXPR:
                if (AST.getChildren().size() > 1) {
                    // numexpr linop term
                    // System.out.println("in NUMEXPR with func: " + current_function);
                    to_return = allocReg(analyzer.getType(AST, current_function));
                    ILRegister numexpr = recursiveGenerate(AST.getChild(0), current_function);
                    ILRegister term = recursiveGenerate(AST.getChild(2), current_function);
                    String linop = AST.getChild(1).getChild(0).getVal();
                    ILInstruction to_emit;
                    if (linop.equals("+")) {
                        to_emit = new ILInstruction(InstructionType.ADD, to_return, numexpr, term);
                    } else {
                        to_emit = new ILInstruction(InstructionType.SUB, to_return, numexpr, term);
                    }
                    emit(to_emit);
                } else {
                    // term
                    to_return = recursiveGenerate(AST.getChild(0), current_function);
                }
                break;
            case TERM:
                if (AST.getChildren().size() > 1) {
                    // term nonlinop factor
                    // System.out.println("in TERM");
                    to_return = allocReg(analyzer.getType(AST, current_function));
                    ILRegister term = recursiveGenerate(AST.getChild(0), current_function);
                    ILRegister factor = recursiveGenerate(AST.getChild(2), current_function);
                    String nonlinop =  AST.getChild(1).getChild(0).getVal();
                    ILInstruction to_emit;
                    if (nonlinop.equals("*")) {
                        to_emit = new ILInstruction(InstructionType.MULT, to_return, term, factor);
                    } else {
                        to_emit = new ILInstruction(InstructionType.DIV, to_return, term, factor);
                    }
                    emit(to_emit);
                } else {
                    // factor
                    to_return = recursiveGenerate(AST.getChild(0), current_function);
                }
                break;
            case FACTOR:
                Symbol f = AST.getChild(0);
                switch(f.getType()) {
                    case CONST:
                        to_return = recursiveGenerate(f.getChild(0), current_function);
                        break;
                    case OPEN_PAREN:
                        // numexpr
                        to_return = recursiveGenerate(AST.getChild(1), current_function);
                        break;
                    case ID:
                        if (AST.getChildren().size() > 1) {
                            // id[numexpr]
                            to_return = load_from_array(AST);
                        } else {
                            to_return = getVarRegister(f.getVal(), current_function);
                        }
                        break;
                }
                break;
            default:
                //System.out.println("In the following node: " + AST);
                for(Symbol child : AST.getChildren()) {
                    recursiveGenerate(child, scope);
                }
        }
        return to_return;
    }



    private int alloc_arr(TypeObj type) {
        // System.out.println("alloc arr with type=" + type);
        if (type.getDepth() == 1) {
            if (type.getType() == Type.FLOAT_ARR) {
                return alloc_floats(type.getTopLayer());
            } else if (type.getType() == Type.INT_ARR) {
                return alloc_ints(type.getTopLayer());
            } else {
                throw new RuntimeException("alloc_arr called on non array TypeObj");
            }
        } else {
            int s = alloc_ints(type.getTopLayer());
            ILRegister rs = allocReg(type);
            ILRegister rj = allocReg(type.elementOf());
            ILRegister ri = allocReg(new TypeObj(Type.INT, 0));
            ILInstruction load_s = new ILInstruction(InstructionType.LDI, rs, s);
            emit(load_s);
            for (int i = 0; i < type.getTopLayer(); i++) {
                int j = alloc_arr(type.elementOf());
                ILInstruction load_i = new ILInstruction(InstructionType.LDI, ri, i);
                ILInstruction load_j = new ILInstruction(InstructionType.LDI, rj, j);
                ILInstruction store_addr = new ILInstruction(InstructionType.STR, rj, rs, ri);
                emit(load_i);
                emit(load_j);
                emit(store_addr);
            }
            return s;
        }
    }

    private int alloc_ints(int count) {
        int to_return = ialloc_num;
        ialloc_num += count;
        if (ialloc_num > MEM_SIZE) {
            throw new RuntimeException("out of int memory to allocate");
        }
        return to_return;
    }

    private int alloc_floats(int count) {
        int to_return = falloc_num;
        falloc_num += count;
        if (falloc_num > MEM_SIZE) {
            throw new RuntimeException("out of float memory to allocate");
        }
        return to_return;
    }

    private String loopStartLabel(int loopNum) {
        return "loop:start:" + loopNum;
    }

    private String loopRunLabel(int loopNum) {
        return "loop:run:" + loopNum;
    }

    private String loopDoneLabel(int loopNum) {
        return "loop:done:" + loopNum;
    }

    private String allocNewOrLabelName() {
        return "or-label:" + or_num++;
    }

    private String allocNewAndLabelName() {
        return "and-label:" + and_num++;
    }

    private void constructBoolJump(Symbol boolexpr, ILInstruction cond_met_label, ILInstruction cond_not_met_label) throws TypeException {
        //emit(new ILInstruction(InstructionType.LABEL, "CHECK BOOLEAN LOGIC"));
        if (boolexpr.getChildren().size() > 1) {
            // boolexpr | clause
            ILInstruction or_label = new ILInstruction(InstructionType.LABEL, allocNewOrLabelName());
            constructBoolJump(boolexpr.getChild(0), cond_met_label, or_label);
            emit(or_label);
            constructBoolJumpClauseHelper(boolexpr.getChild(2), cond_met_label, cond_not_met_label);

        } else {
            // clause
            constructBoolJumpClauseHelper(boolexpr.getChild(0), cond_met_label, cond_not_met_label);
        }
    }

    private void constructBoolJumpClauseHelper(Symbol clause, ILInstruction cond_met_label, ILInstruction cond_not_met_label) throws TypeException {
        if (clause.getChildren().size() > 1) {
            // clause & pred
            ILInstruction and_label = new ILInstruction(InstructionType.LABEL, allocNewAndLabelName());
            constructBoolJumpClauseHelper(clause.getChild(0), and_label, cond_not_met_label);
            emit(and_label);
            constructBoolJumpPredHelper(clause.getChild(2), cond_met_label, cond_not_met_label);
        } else {
            constructBoolJumpPredHelper(clause.getChild(0), cond_met_label, cond_not_met_label);
        }
    }

    private void constructBoolJumpPredHelper(Symbol pred, ILInstruction cond_met_label, ILInstruction cond_not_met_label) throws TypeException {
        if (pred.getChild(0).getType() == SymbolType.NUMEXPR) {
            // numexpr boolop numexpr
            ILRegister left = recursiveGenerate(pred.getChild(0), current_function);
            ILRegister right = recursiveGenerate(pred.getChild(2), current_function);
            InstructionType compare_op;
            switch(pred.getChild(1).getChild(0).getType()) {
                case EQUALS:
                    compare_op = InstructionType.BREQ;
                    break;
                case NOT_EQUALS:
                    compare_op = InstructionType.BRNEQ;
                    break;
                case LESS_THAN:
                    compare_op = InstructionType.BRLT;
                    break;
                case GREATER_THAN:
                    compare_op = InstructionType.BRGT;
                    break;
                case LESS_THAN_EQUAL:
                    compare_op = InstructionType.BRLEQ;
                    break;
                case GREATER_THAN_EQUAL:
                    compare_op = InstructionType.BRGEQ;
                    break;
                default:
                    throw new RuntimeException("boolop is not one of the allowed values: got " + pred.getChild(1));
            }
            ILInstruction branch_on_cond_met = new ILInstruction(compare_op, left, right, cond_met_label.getLabelName());
            emit(branch_on_cond_met);
            emit(new ILInstruction(InstructionType.GOTO, cond_not_met_label.getLabelName()));
        } else {
            // (boolexpr)
            constructBoolJump(pred.getChild(1), cond_met_label, cond_not_met_label);
        }
    }


    private String ifCondMetLabelName(int ifNum) {
        return "if:cond_met:" + ifNum;
    }

    private String ifCondNotMetLabelName(int ifNum) {
        return "if:cond_not_met:" + ifNum;
    }

    private String ifDoneLabelName(int ifNum) {
        return "if:done:" + ifNum;
    }

    private ILRegister load_from_array(Symbol factor) throws TypeException {
        ILRegister src_reg = getVarRegister(factor.getChild(0).getVal(), current_function);
        TypeObj new_type = src_reg.getType().elementOf();
        ILRegister dst_reg = allocReg(new_type);
        ILRegister offset_reg = recursiveGenerate(factor.getChild(2), current_function);
        ILInstruction ldr = new ILInstruction(InstructionType.LDR, dst_reg, src_reg, offset_reg);
        emit(ldr);
        return dst_reg;
    }

    private void assign_to_lvalue(Symbol lvalue, ILRegister rvalue) throws TypeException {
        Symbol id = lvalue.getChild(0);
        Symbol optoffset = lvalue.getChild(1);
        if (optoffset.getChildren().size() > 0) {
            // indexing into an array, need to store
            ILRegister base_reg = getVarRegister(id.getVal(), current_function);
            ILRegister offset_reg = recursiveGenerate(optoffset.getChild(1), current_function);
            ILInstruction str = new ILInstruction(InstructionType.STR, rvalue, base_reg, offset_reg);
            emit(str);
        } else {
            // just a variable
            ILRegister lreg = getVarRegister(id.getVal(), current_function);
            ILInstruction mov = new ILInstruction(InstructionType.MOV, lreg, rvalue);
            emit(mov);
        }
    }

    /**
     * Add an instruction to the generated list of instructions for the program.
     * @param the instruction to add
    */
    private void emit(ILInstruction instruction) {
        this.instructions.add(instruction);
    }

    /**
     * Get the register corresponding to a variable.
    */
    private ILRegister getVarRegister(String varname, String functionName) {
        if (functionName != null) {
            if (param_regs.get(functionName).get(varname) != null) {
                return param_regs.get(functionName).get(varname);
            }
        }
        return global_variable_regs.get(varname);
    }

    private ILRegister allocReg(TypeObj type) {
        ILRegister to_return = new ILRegister("r" + next_regno, type);
        next_regno++;
        return to_return;
    }

    private ILRegister allocNewVar(String varname, String functionName, TypeObj type) {
        ILRegister new_reg = allocReg(type);
        if (getVarRegister(varname, functionName) != null) {
            throw new RuntimeException("Tried to allocate a new variable that was already declared " + varname + " in function " + functionName);
        }
        if (functionName == null) {
            global_variable_regs.put(varname, new_reg);
        } else {
            Map<String, ILRegister> func_params = param_regs.get(functionName);
            if (func_params == null) {
                func_params = new HashMap<String, ILRegister>();
                param_regs.put(functionName, func_params);
            }
            func_params.put(varname, new_reg);
        }
        return new_reg;
    }
}