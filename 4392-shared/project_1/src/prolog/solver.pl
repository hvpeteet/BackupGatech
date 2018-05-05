#!/usr/local/bin/swipl -f -q

%% =================================
%% Printing logic
%% =================================

:- initialization main.

main :-
  current_prolog_flag(argv, Argv),
  [H|_] = Argv,
  atom_number(H, N),
  %% magic happens
  fewestStepsSolution(N, _, Trace),
  showTrace(Trace),
  write('\n'),
  halt(0).

showStep(R, C, M) :-
    write('\n'),
    write(R),
    write(' '),
    write(C),
    write(' '),
    write(M).

showTrace([]).

showTrace([R, C, M, Trace]) :-
    showTrace(Trace),
    showStep(R ,C, M).

move(left, 0, -1).
move(up-left, -1, -1).
move(up-right, -1, 0).
move(right, 0, 1).
move(down-right, 1, 1).
move(down-left, 1, 0).

%% =================================
%% Basic Helpers to construct boards
%% =================================

%% Check peg value at [Row, Col]
peg(Board, Row, Col, Peg) :-
    nth0(Row, Board, R, _),
    nth0(Col, R, Peg, _).

%% Create a row of all 1s of a set "Size"
rowOfOnes(1, [1]).
rowOfOnes(Size, Row) :-
    Size > 1,
    S1 is Size - 1,
    rowOfOnes(S1, R1),
    append(R1,[1],Row).

%% A N x N board that has every hole filled
baseBoard(1, [[1]]).
baseBoard(N, Board) :-
    N > 1,
    N1 is N - 1,
    baseBoard(N1, B),
    rowOfOnes(N, R),
    append(B, [R], Board).

%% A N x N board with only 1 hole that is empty
%% Given a Board Size it will give you a board and tell you the row and column of the peg that is empty.
startBoard(BoardSize, StartBoard, R, C) :-
    baseBoard(BoardSize, Board),
    B is BoardSize - 1,
    between(0, B, R),
    between(0, R, C),
    replace2D(Board, R, C, 0, StartBoard).

%% =========================================
%% Basic Helpers for multi-dimensional lists
%% =========================================

%% 2-D array replacement
replace2D([], _, _, _, []).

replace2D([H|Tail], 0, ColIndex, NewVal, [NewH|Tail]) :-
    replace1D(H, ColIndex, NewVal, NewH).

replace2D([H|Tail], RowIndex, ColIndex, NewVal, [H|NewTail]) :-
    dif(RowIndex, 0),
    R1 is RowIndex-1,
    replace2D(Tail, R1, ColIndex, NewVal, NewTail).


%% 1-D array replacement
replace1D([], _, _, []).

replace1D([_|Tail], 0, NewVal, [NewVal|Tail]).

replace1D([H|Tail], Index, NewVal, [H|NTail]) :-
    dif(Index, 0),
    Index1 is Index-1,
    replace1D(Tail, Index1, NewVal, NTail).

%% =================================
%% Main logic
%% =================================

%% R0 and C0 are the row and column to start from.
isLegalMove(Board, BoardSize, R0, C0, Move, ResultBoard) :-
    % Get 3 locations
    move(Move, Dr, Dc),

    MaxRow is BoardSize - 1,
    between(0, MaxRow, R0),
    between(0, R0, C0),

    R1 is R0 + Dr,
    R2 is R1 + Dr,

    C1 is C0 + Dc,
    C2 is C1 + Dc,

    % Check bounds
    R0 >= 0,
    R2 >= 0,
    R0 < BoardSize,
    R2 < BoardSize,

    C0 >= 0,
    C2 >= 0,
    C0 =< R0,
    C2 =< R2,

    % Check if the original peg exists
    peg(Board, R0, C0, 1),
    % Check if middle peg exists
    peg(Board, R1, C1, 1),
    % Make sure target peg does not exist
    peg(Board, R2, C2, 0),

    % Restrict the new Board
    replace2D(Board, R0, C0, 0, NewBoard0),
    replace2D(NewBoard0, R1, C1, 0, NewBoard1),
    replace2D(NewBoard1, R2, C2, 1, ResultBoard).

% If a board is reachable from another in a set number of moves.
reachableInNMoves(StartBoard, _, 0, StartBoard, []).

reachableInNMoves(StartBoard, BoardSize, NumMoves, ReachableBoard, [R, C, Move, Trace]) :-
    dif(NumMoves, 0),
    OneLessMove is NumMoves - 1,
    reachableInNMoves(StartBoard, BoardSize, OneLessMove, WasReachable, Trace),
    isLegalMove(WasReachable, BoardSize, R, C, Move, ReachableBoard).

% If a board has no more valid moves.
noMoreMoves(Board, BoardSize) :-
    (reachableInNMoves(Board, BoardSize, 1, _, _) -> false; true).

reachableSolutionInNMoves(BoardSize, NumMoves, ResultBoard, Trace) :-
    startBoard(BoardSize, StartBoard, R, C),
    reachableInNMoves(StartBoard, BoardSize, NumMoves, ResultBoard, Trace),
    noMoreMoves(ResultBoard, BoardSize),
    write(R),
    write(' '),
    write(C).

fewestStepsSolution(BoardSize, N, Trace) :-
    B is BoardSize * BoardSize,
    between(0, B, N),
    reachableSolutionInNMoves(BoardSize, N, _, Trace).
