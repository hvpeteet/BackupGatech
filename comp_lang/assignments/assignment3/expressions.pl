%% ----------------------
%% Expression Interpreter
%% ----------------------

%% Is integer
eval(E, E):-
    integer(E).
%% Is variable
eval(E, N) :-
    ctx(E, N).
%% add
eval(add(A,B), N) :-
    eval(A, N1),
    eval(B, N2),
    N is N1 + N2.
%% mult
eval(mult(A,B), N):-
    eval(A, N1),
    eval(B, N2),
    N is N1 * N2.