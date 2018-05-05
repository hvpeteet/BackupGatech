package compiler.shared;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public enum SymbolType {
    // Keywords, literals, non-terminals
    // see language_spec.pdf in 4240 root folder
    ARRAY,
    BEGIN,
    BREAK,
    DO,
    ELSE,
    END,
    ENDDO,
    ENDIF,
    FLOAT,
    FOR,
    FUNC,
    IF,
    IN,
    INT,
    LET,
    OF,
    RETURN,
    THEN,
    TO,
    TYPE,
    VAR,
    WHILE,
    COMMA,
    COLON,
    SEMI_COLON,
    OPEN_PAREN,
    CLOSE_PAREN,
    OPEN_BRACE,
    CLOSE_BRACE,
    OPEN_CURLY,
    CLOSE_CURLY,
    PERIOD,
    PLUS,
    MINUS,
    STAR,
    SLASH,
    EQUALS,
    NOT_EQUALS,
    LESS_THAN,
    GREATER_THAN,
    LESS_THAN_EQUAL,
    GREATER_THAN_EQUAL,
    AND,
    OR,
    COLON_EQ,

    EOF,
    EPSILON,
    START,

    ID,
    INT_LIT,
    FLOAT_LIT,

    PROGRAM,
    DECLSEG,
    TYPEDECLS,
    TYPEDECL,
    VAR_TYPE,
    VARDECLS,
    VARDECL,
    IDS,
    OPTINIT,
    FUNCDECLS,
    FUNCDECL,
    PARAMS,
    NEPARAMS,
    PARAM,
    OPTRETTYPE,
    STMTS,
    FULLSTMT,
    STMT,
    LVALUE,
    OPTOFFSET,
    OPTSTORE,
    NUMEXPRS,
    NEEXPRS,
    BOOLEXPR,
    CLAUSE,
    PRED,
    BOOLOP,
    NUMEXPR,
    LINOP,
    TERM,
    NONLINOP,
    FACTOR,
    CONST,

    OPTSTORE_FIX_0,
    OPTSTORE_FIX_1;

    private SymbolType fix;
    private boolean isTerm;
    private String regex;
    public SymbolType[][] derivations;
    private static Map<SymbolType, Set<SymbolType>> firsts;

    public boolean isTerm() {
        return this.isTerm;
    }

    public String regex() {
        return this.regex;
    }

    public SymbolType getFix() {
        return this.fix;
    }

    /**
     * Get the firsts of a SymbolType.
     * @return a set of firsts for the given symbol
    */
    public Set<SymbolType> firsts() {
        if (SymbolType.firsts == null) {
            initializeFirsts();
        }
        return SymbolType.firsts.get(this);
    }

    private static void initializeFirsts() {
        SymbolType.firsts = new HashMap<SymbolType, Set<SymbolType>>();
        for (SymbolType symbol : SymbolType.values()) {
            SymbolType.firsts.put(symbol, new HashSet<SymbolType>());
            if (symbol.isTerm()) {
                SymbolType.firsts.get(symbol).add(symbol);
            }
        }
        boolean changed = true;
        while (changed) {
            changed = false;
            for (SymbolType symbol : SymbolType.values()) {
                if (!symbol.isTerm()) {
                    Set firstsSet = SymbolType.firsts.get(symbol);
                    int oldSize = firstsSet.size();
                    if (symbol.derivations == null) {
                        throw new RuntimeException("Symbol " + symbol + " is not terminal, but also has no derivations");
                    }
                    for (SymbolType[] rule : symbol.derivations) {
                        SymbolType first = rule[0];
                        if (first.isTerm()) {
                            firstsSet.add(first);
                        } else {
                            firstsSet.addAll(firsts.get(first));
                        }
                    }
                    if (oldSize < firstsSet.size()) {
                        changed = true;
                    }
                }
            }
        }
    }

    /**
     * A representation of this var type according to language specs.
     * @return a representation of this var matching the format in the PDF.
    */
    public String toString() {
        if (this == VAR_TYPE) {
            return "type";
        }
        return this.name().toLowerCase();
    }

    // Keywords
    static {
        ARRAY.isTerm = true;
        ARRAY.derivations = null;
        ARRAY.regex = "array";

        BEGIN.isTerm = true;
        BEGIN.derivations = null;
        BEGIN.regex = "begin";

        BREAK.isTerm = true;
        BREAK.derivations = null;
        BREAK.regex = "break";

        DO.isTerm = true;
        DO.derivations = null;
        DO.regex = "do";

        ELSE.isTerm = true;
        ELSE.derivations = null;
        ELSE.regex = "else";

        END.isTerm = true;
        END.derivations = null;
        END.regex = "end";

        ENDDO.isTerm = true;
        ENDDO.derivations = null;
        ENDDO.regex = "enddo";

        ENDIF.isTerm = true;
        ENDIF.derivations = null;
        ENDIF.regex = "endif";

        FLOAT.isTerm = true;
        FLOAT.derivations = null;
        FLOAT.regex = "float";

        FOR.isTerm = true;
        FOR.derivations = null;
        FOR.regex = "for";

        FUNC.isTerm = true;
        FUNC.derivations = null;
        FUNC.regex = "func";

        IF.isTerm = true;
        IF.derivations = null;
        IF.regex = "if";

        IN.isTerm = true;
        IN.derivations = null;
        IN.regex = "in";

        INT.isTerm = true;
        INT.derivations = null;
        INT.regex = "int";

        LET.isTerm = true;
        LET.derivations = null;
        LET.regex = "let";

        OF.isTerm = true;
        OF.derivations = null;
        OF.regex = "of";

        RETURN.isTerm = true;
        RETURN.derivations = null;
        RETURN.regex = "return";

        THEN.isTerm = true;
        THEN.derivations = null;
        THEN.regex = "then";

        TO.isTerm = true;
        TO.derivations = null;
        TO.regex = "to";

        TYPE.isTerm = true;
        TYPE.derivations = null;
        TYPE.regex = "type";

        VAR.isTerm = true;
        VAR.derivations = null;
        VAR.regex = "var";

        WHILE.isTerm = true;
        WHILE.derivations = null;
        WHILE.regex = "while";

        COMMA.isTerm = true;
        COMMA.derivations = null;
        COMMA.regex = ",";

        COLON.isTerm = true;
        COLON.derivations = null;
        COLON.regex = ":";

        SEMI_COLON.isTerm = true;
        SEMI_COLON.derivations = null;
        SEMI_COLON.regex = ";";

        OPEN_PAREN.isTerm = true;
        OPEN_PAREN.derivations = null;
        OPEN_PAREN.regex = "(";

        CLOSE_PAREN.isTerm = true;
        CLOSE_PAREN.derivations = null;
        CLOSE_PAREN.regex = ")";

        OPEN_BRACE.isTerm = true;
        OPEN_BRACE.derivations = null;
        OPEN_BRACE.regex = "\\[";

        CLOSE_BRACE.isTerm = true;
        CLOSE_BRACE.derivations = null;
        CLOSE_BRACE.regex = "\\]";

        OPEN_CURLY.isTerm = true;
        OPEN_CURLY.derivations = null;
        OPEN_CURLY.regex = "{";

        CLOSE_CURLY.isTerm = true;
        CLOSE_CURLY.derivations = null;
        CLOSE_CURLY.regex = "}";

        PERIOD.isTerm = true;
        PERIOD.derivations = null;
        PERIOD.regex = ".";

        PLUS.isTerm = true;
        PLUS.derivations = null;
        PLUS.regex = "+";

        MINUS.isTerm = true;
        MINUS.derivations = null;
        MINUS.regex = "\\-";

        STAR.isTerm = true;
        STAR.derivations = null;
        STAR.regex = "\\*";

        SLASH.isTerm = true;
        SLASH.derivations = null;
        SLASH.regex = "/";

        EQUALS.isTerm = true;
        EQUALS.derivations = null;
        EQUALS.regex = "=";

        NOT_EQUALS.isTerm = true;
        NOT_EQUALS.derivations = null;
        NOT_EQUALS.regex = "<>";

        LESS_THAN.isTerm = true;
        LESS_THAN.derivations = null;
        LESS_THAN.regex = "<";

        GREATER_THAN.isTerm = true;
        GREATER_THAN.derivations = null;
        GREATER_THAN.regex = ">";

        LESS_THAN_EQUAL.isTerm = true;
        LESS_THAN_EQUAL.derivations = null;
        LESS_THAN_EQUAL.regex = "<=";

        GREATER_THAN_EQUAL.isTerm = true;
        GREATER_THAN_EQUAL.derivations = null;
        GREATER_THAN_EQUAL.regex = ">=";

        AND.isTerm = true;
        AND.derivations = null;
        AND.regex = "&";

        OR.isTerm = true;
        OR.derivations = null;
        OR.regex = "\\|";

        COLON_EQ.isTerm = true;
        COLON_EQ.derivations = null;
        COLON_EQ.regex = ":=";

        EOF.isTerm = true;
        EOF.derivations = null;
        EOF.regex = "";           // Not sure

        EPSILON.isTerm = true;
        EPSILON.derivations = null;
        EPSILON.regex = "";

    }

    // Literals
    // regex operations supported: *,[],-,|
    // don't use other operations in regex (parentheses, ., ?, +, etc.)
    static {
        ID.isTerm = true;
        ID.derivations = null;
        ID.regex = "__*[a-zA-Z0-9][_a-zA-Z0-9]*|[a-zA-Z][_a-zA-Z0-9]*";

        INT_LIT.isTerm = true;
        INT_LIT.derivations = null;
        INT_LIT.regex = "[0-9][0-9]*";

        FLOAT_LIT.isTerm = true;
        FLOAT_LIT.derivations = null;
        FLOAT_LIT.regex = "0.[0-9]*|[1-9][0-9]*.[0-9]*";
    }

    // Non-Terminals
    static {
        PROGRAM.isTerm = false;
        PROGRAM.regex = null;
        PROGRAM.derivations = new SymbolType[][] {{LET, DECLSEG, IN, STMTS, END}};

        DECLSEG.isTerm = false;
        DECLSEG.regex = null;
        DECLSEG.derivations = new SymbolType[][] {{TYPEDECLS, VARDECLS, FUNCDECLS}};

        TYPEDECLS.isTerm = false;
        TYPEDECLS.regex = null;
        TYPEDECLS.derivations = new SymbolType[][] {
            {EPSILON},
            {TYPEDECL, TYPEDECLS}
        };

        TYPEDECL.isTerm = false;
        TYPEDECL.regex = null;
        TYPEDECL.derivations = new SymbolType[][] {{TYPE, ID, COLON_EQ, VAR_TYPE, SEMI_COLON}};

        VAR_TYPE.isTerm = false;
        VAR_TYPE.regex = null;
        VAR_TYPE.derivations = new SymbolType[][] {
            {INT},
            {FLOAT},
            {ID},
            {ARRAY, OPEN_BRACE, INT_LIT, CLOSE_BRACE, OF, VAR_TYPE}
        };

        VARDECLS.isTerm = false;
        VARDECLS.regex = null;
        VARDECLS.derivations = new SymbolType[][] {
            {EPSILON},
            {VARDECL, VARDECLS}
        };

        VARDECL.isTerm = false;
        VARDECL.regex = null;
        VARDECL.derivations = new SymbolType[][] {{VAR, IDS, COLON, VAR_TYPE, OPTINIT, SEMI_COLON}};

        IDS.isTerm = false;
        IDS.regex = null;
        IDS.derivations = new SymbolType[][] {
            {ID},
            {ID, COMMA, IDS}
        };

        OPTINIT.isTerm = false;
        OPTINIT.regex = null;
        OPTINIT.derivations = new SymbolType[][] {
            {EPSILON},
            {COLON_EQ, CONST}
        };

        FUNCDECLS.isTerm = false;
        FUNCDECLS.regex = null;
        FUNCDECLS.derivations = new SymbolType[][] {
            {EPSILON},
            {FUNCDECL, FUNCDECLS}
        };

        FUNCDECL.isTerm = false;
        FUNCDECL.regex = null;
        FUNCDECL.derivations = new SymbolType[][] {
            {FUNC, ID, OPEN_PAREN, PARAMS, CLOSE_PAREN, OPTRETTYPE, BEGIN, STMTS, END, SEMI_COLON}
        };

        PARAMS.isTerm = false;
        PARAMS.regex = null;
        PARAMS.derivations = new SymbolType[][] {
            {EPSILON},
            {NEPARAMS}
        };

        NEPARAMS.isTerm = false;
        NEPARAMS.regex = null;
        NEPARAMS.derivations = new SymbolType[][] {
            {PARAM},
            {PARAM, COMMA, NEPARAMS}
        };

        PARAM.isTerm = false;
        PARAM.regex = null;
        PARAM.derivations = new SymbolType[][] {
            {ID, COLON, VAR_TYPE}
        };

        OPTRETTYPE.isTerm = false;
        OPTRETTYPE.regex = null;
        OPTRETTYPE.derivations = new SymbolType[][] {
            {EPSILON},
            {COLON, VAR_TYPE}
        };

        STMTS.isTerm = false;
        STMTS.regex = null;
        STMTS.derivations = new SymbolType[][] {
            {FULLSTMT},
            {FULLSTMT, STMTS}
        };

        FULLSTMT.isTerm = false;
        FULLSTMT.regex = null;
        FULLSTMT.derivations = new SymbolType[][] {
            {STMT, SEMI_COLON}
        };

        STMT.isTerm = false;
        STMT.regex = null;
        STMT.derivations = new SymbolType[][] {
            {LVALUE, COLON_EQ, NUMEXPR},
            {IF, BOOLEXPR, THEN, STMTS, ENDIF},
            {IF, BOOLEXPR, THEN, STMTS, ELSE, STMTS, ENDIF},
            {WHILE, BOOLEXPR, DO, STMTS, ENDDO},
            {FOR, ID, COLON_EQ, NUMEXPR, TO, NUMEXPR, DO, STMTS, ENDDO},
            {OPTSTORE_FIX_1},
            {OPTSTORE_FIX_0},
            {BREAK},
            {RETURN, NUMEXPR}
        };

        OPTSTORE_FIX_0.fix = OPTSTORE;
        OPTSTORE_FIX_0.isTerm = false;
        OPTSTORE_FIX_0.regex = null;
        OPTSTORE_FIX_0.derivations = new SymbolType[][] {
            {ID, OPEN_PAREN, NUMEXPRS, CLOSE_PAREN}
        };

        OPTSTORE_FIX_1.fix = OPTSTORE;
        OPTSTORE_FIX_1.isTerm = false;
        OPTSTORE_FIX_1.regex = null;
        OPTSTORE_FIX_1.derivations = new SymbolType[][] {
            {LVALUE, COLON_EQ, ID, OPEN_PAREN, NUMEXPRS, CLOSE_PAREN}
        };



        LVALUE.isTerm = false;
        LVALUE.regex = null;
        LVALUE.derivations = new SymbolType[][] {
            {ID, OPTOFFSET}
        };

        OPTOFFSET.isTerm = false;
        OPTOFFSET.regex = null;
        OPTOFFSET.derivations = new SymbolType[][] {
            {EPSILON},
            {OPEN_BRACE, NUMEXPR, CLOSE_BRACE}
        };

        OPTSTORE.isTerm = false;
        OPTSTORE.regex = null;
        OPTSTORE.derivations = new SymbolType[][] {
            {LVALUE, COLON_EQ}
        };

        NUMEXPRS.isTerm = false;
        NUMEXPRS.regex = null;
        NUMEXPRS.derivations = new SymbolType[][] {
            {EPSILON},
            {NEEXPRS}
        };

        NEEXPRS.isTerm = false;
        NEEXPRS.regex = null;
        NEEXPRS.derivations = new SymbolType[][] {
            {NUMEXPR},
            {NUMEXPR, COMMA, NEEXPRS}
        };

        BOOLEXPR.isTerm = false;
        BOOLEXPR.regex = null;
        BOOLEXPR.derivations = new SymbolType[][] {
            {CLAUSE},
            {BOOLEXPR, OR, CLAUSE}
        };

        CLAUSE.isTerm = false;
        CLAUSE.regex = null;
        CLAUSE.derivations = new SymbolType[][] {
            {PRED},
            {PRED, AND, CLAUSE}
        };

        PRED.isTerm = false;
        PRED.regex = null;
        PRED.derivations = new SymbolType[][] {
            {NUMEXPR, BOOLOP, NUMEXPR},
            {OPEN_PAREN, BOOLEXPR, CLOSE_PAREN}
        };

        BOOLOP.isTerm = false;
        BOOLOP.regex = null;
        BOOLOP.derivations = new SymbolType[][] {
            {EQUALS},
            {NOT_EQUALS},
            {LESS_THAN_EQUAL},
            {GREATER_THAN_EQUAL},
            {LESS_THAN},
            {GREATER_THAN}
        };

        NUMEXPR.isTerm = false;
        NUMEXPR.regex = null;
        NUMEXPR.derivations = new SymbolType[][] {
            {TERM},
            {NUMEXPR, LINOP, TERM}
        };

        LINOP.isTerm = false;
        LINOP.regex = null;
        LINOP.derivations = new SymbolType[][] {
            {PLUS},
            {MINUS}
        };

        TERM.isTerm = false;
        TERM.regex = null;
        TERM.derivations = new SymbolType[][] {
            {FACTOR},
            {TERM, NONLINOP, FACTOR}
        };

        NONLINOP.isTerm = false;
        NONLINOP.regex = null;
        NONLINOP.derivations = new SymbolType[][] {
            {STAR},
            {SLASH}
        };

        FACTOR.isTerm = false;
        FACTOR.regex = null;
        FACTOR.derivations = new SymbolType[][] {
            {CONST},
            {ID},
            {ID, OPEN_BRACE, NUMEXPR, CLOSE_BRACE},
            {OPEN_PAREN, NUMEXPR, CLOSE_PAREN}
        };

        CONST.isTerm = false;
        CONST.regex = null;
        CONST.derivations = new SymbolType[][] {
            {INT_LIT},
            {FLOAT_LIT}
        };

        START.isTerm = false;
        START.regex = null;
        START.derivations = new SymbolType[][] {
            {PROGRAM}
        };
    }
}