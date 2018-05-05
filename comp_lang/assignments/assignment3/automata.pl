%% ----------------
%% Finite Automata
%% ----------------

accepts(L):-
    reverse(Lrev, L),
    accepting(E),
    reachable(Lrev, E).

%% can reach state S from a list of inputs
reachable([], S, []):-
    init(S).
reachable([H|T], S):-
    delta(S1, H, S),
    reachable(T, S1).
