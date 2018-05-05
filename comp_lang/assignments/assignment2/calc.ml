(*Part 1: Define a type calc_var *)
type calc_var = string ;;

(* Part 2: Define calc_exp and calc_ctx*)
type calc_ctx = calc_var -> int ;;

type calc_exp =
    VAL of int
    | VAR of calc_var
    | ADD of calc_exp * calc_exp
    | MULT of calc_exp * calc_exp;;

let rec exp2string exp =
    (match exp with
        | VAL x -> string_of_int(x)
        | VAR x -> x
        | ADD (x, y) -> String.concat " " ["("; exp2string x; "+"; exp2string y;")"]
        | MULT (x, y) -> String.concat " " ["("; exp2string x; "*"; exp2string y;")"]
    );;

(* Part 3: Give an interpreter *)
type val_list = (string * int) list;;

let rec basic_ctx val_list x =
    (match val_list with
        | [] -> raise(Failure "x not in list")
        | (a,b)::l' -> if a = x then b else basic_ctx l' x
    );;

let rec interp ctx exp =
    (match exp with
        | VAL i -> i
        | VAR v -> ctx v
        | ADD (a, b) -> (interp ctx a) + (interp ctx b)
        | MULT (a, b) -> (interp ctx a) * (interp ctx b)
    );;

print_string("========== interpreter[a=3 b=5] =========\n");;

(* Example 1 *)
let ctx = basic_ctx [("a", 3)] in
    let exp = MULT ((VAR "a"),(VAR "a")) in
        print_string(exp2string exp);
        print_string("-->");
        print_int(interp ctx exp);
        print_string("\n");;

(* Example 2 *)
let ctx = basic_ctx [("a", 3)] in
    let exp = ADD ((VAL 27),(VAR "a")) in
        print_string(exp2string exp);
        print_string("-->");
        print_int(interp ctx exp);;
print_string("\n");;

(* Example 3 *)
let ctx = basic_ctx [("a", 3); ("b", 5)] in
    let exp = MULT ((VAR "b"),(ADD ((VAR "a"),(VAL 2)))) in
        print_string(exp2string exp);
        print_string("-->");
        print_int(interp ctx exp);;
print_string("\n");;

(* Optimization of e + 0 & 3 examples *)
let rec bottom_up ast f =
    (match ast with
        | VAL(x) -> VAL(x)
        | VAR(x) -> VAR(x)
        | ADD(x,y) -> f (ADD((bottom_up x f),(bottom_up y f)))
        | MULT(x,y) -> f (MULT((bottom_up x f),(bottom_up y f)))
    );;

let zero_add_opt_single exp =
    (match exp with
        | ADD(x, (VAL(0))) -> x
        | ADD((VAL(0)),x) -> x
        | x -> x
    );;

let zero_add_opt exp =
    bottom_up exp zero_add_opt_single;;

let print_test_case exp opt =
    print_string(exp2string exp);
    print_string("-->");
    print_string(exp2string (opt exp));
    print_string("\n");;


print_string("========== e + 0 =========\n");;
(* Example 1 *)
print_test_case (ADD(VAR("a"),VAL(0))) zero_add_opt;;

(* Example 2 *)
print_test_case (ADD(VAR("b"),ADD(VAL(0),VAL(0)))) zero_add_opt;;

(* Example 3 (Only handles right hand side zeros) *)
print_test_case (ADD(VAR("c"),ADD(VAL(0),VAL(2)))) zero_add_opt;;

(* Optimization of e * 0 & 3 examples *)
let zero_mult_opt_single exp =
    (match exp with
        | MULT(x,(VAL(0))) -> VAL(0)
        | MULT((VAL(0)), x) -> VAL(0)
        | x -> x
    );;

let zero_mult_opt exp =
    bottom_up exp zero_mult_opt_single;;

print_string("========== e * 0 =========\n");;
(* Example 1 *)
print_test_case (MULT(VAR("a"),VAL(0))) zero_mult_opt;;

(* Example 2 *)
print_test_case (MULT(VAR("b"),MULT(VAL(0),VAR("b")))) zero_mult_opt;;

(* Example 3 (Only handles right hand side zeros) *)
print_test_case (MULT(VAR("c"),MULT(VAL(0),VAL(2)))) zero_mult_opt;;

(* Constant folding & 3 examples *)

let const_fold_opt_single exp =
    (match exp with
        | MULT(VAL(x),VAL(y)) -> VAL(x*y)
        | ADD(VAL(x),VAL(y)) -> VAL(x+y)
        | x -> x
    );;

let const_fold_opt exp =
    bottom_up exp const_fold_opt_single;;

print_string("========== folding =========\n");;
(* Example 1 *)
print_test_case (MULT(VAR("a"),ADD(VAL(7),VAL(3)))) const_fold_opt;;

(* Example 2 *)
print_test_case (MULT(VAR("b"),MULT(VAL(1),VAL(0)))) const_fold_opt;;

(* Example 3 (Only handles right hand side zeros) *)
print_test_case (MULT(VAR("c"),MULT(VAL(7),VAL(2)))) const_fold_opt;;

(* Iteratively combine optimizations & 3 examples *)
print_string("========== it all comes together (1 iterations) =========\n");;

let all_opt exp =
    let raddzero = (bottom_up exp zero_add_opt) in
    let rmultzero = (bottom_up raddzero zero_mult_opt) in
        bottom_up rmultzero const_fold_opt;;

(* Example 1 *)
print_test_case (ADD(VAR("a"),MULT(VAR("b"),ADD(VAL(1),VAL(-1))))) all_opt;;

(* Example 2 *)
print_test_case (ADD(MULT(VAR("a"),VAL(0)),MULT(VAR("b"),ADD(VAL(1),VAL(-1))))) all_opt;;

(* Example 3 *)
print_test_case (ADD(ADD(VAR("a"),MULT(VAL(0),VAL(0))),MULT(VAR("b"),ADD(VAL(1),VAL(-1))))) all_opt;;

print_string("========== it all comes together (N iterations) [same examples as above] =========\n");;

let rec all_opt_iter exp =
    let e = all_opt exp in
        if e = exp then e else all_opt_iter e;;

(* Example 1 *)
print_test_case (ADD(VAR("a"),MULT(VAR("b"),ADD(VAL(1),VAL(-1))))) all_opt_iter;;

(* Example 2 *)
print_test_case (ADD(MULT(VAR("a"),VAL(0)),MULT(VAR("b"),ADD(VAL(1),VAL(-1))))) all_opt_iter;;

(* Example 3 *)
print_test_case (ADD(ADD(VAR("a"),MULT(VAL(0),VAL(0))),MULT(VAR("b"),ADD(VAL(1),VAL(-1))))) all_opt_iter;;
