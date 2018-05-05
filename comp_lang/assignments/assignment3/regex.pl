%% ----------------------
%% Regex
%% ----------------------
match(eps, []).
match(null, _):-
    false.
match(A, [A]).

%% recat
match(recat(R0, R1), L):-
    append(H, T, L),
    match(R0, H),
    match(R1, T).

%% alt
match(alt(R0, _), S):-
    match(R0, S).

match(alt(_, R1), S):-
    match(R1, S).

%% kleene
match(kleene(_), []).
match(kleene(R), S):-
    append(A, B, S),
    match(R, A),
    match(kleene(R), B).