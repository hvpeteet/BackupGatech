open Printf
open Async.Std
open Core

type move = Start | Right | Left | Up_Right | Up_Left | Down_Right | Down_Left;;

type board_move = {
	move: move;
	idx: int;
};;

type board_state = {
	pegs: int array;
	history: board_move list;
};;

let to_col_row idx =
	let rec to_col idx row =
		if idx > row then
			to_col (idx - row - 1) (row + 1)
		else
			(idx, row)
	in
	to_col idx 0;;

let rec to_idx (col, row) =
	if row > 0 then
		to_idx (col + row, row - 1)
	else
		col;;

let get_move_pos (col, row) move =
	match move with
		| Start -> raise (Failure "This should never happen!")
		| Right -> (col + 1, row)
		| Left -> (col - 1, row)
		| Up_Right -> (col, row - 1)
		| Up_Left -> (col - 1, row - 1)
		| Down_Right -> (col + 1, row + 1)
		| Down_Left -> (col, row + 1);;

let make_move state idx move acc =
	if state.pegs.(idx) == 0 then
		acc
	else
		let is_oob (col, row) = col < 0 || row < 0 || (to_idx (col, row)) >= Array.length state.pegs || col > row in
		let pos = to_col_row idx in
		let next_pos = get_move_pos pos move in
		let next_idx = to_idx next_pos in
		let next_next_pos = get_move_pos next_pos move in
		let next_next_idx = to_idx next_next_pos in

		if is_oob pos || is_oob next_pos || is_oob next_next_pos ||
			state.pegs.(next_idx) == 0 || state.pegs.(next_next_idx) == 1 then
			acc
		else
			let new_pegs = Array.copy state.pegs in
			let () = new_pegs.(idx) <- 0; new_pegs.(next_idx) <- 0; new_pegs.(next_next_idx) <- 1 in
			let new_history = {move = move; idx = idx} :: state.history in
			(Deferred.return ({ pegs = new_pegs; history = new_history })) :: acc;;

let get_next_states state =
	let rec try_peg idx =
		if idx < Array.length state.pegs then
			List.rev_append (make_move state idx Right
								(make_move state idx Left
								(make_move state idx Up_Right
								(make_move state idx Up_Left
								(make_move state idx Down_Right
								(make_move state idx Down_Left []))))))
							(try_peg (idx + 1))
		else
			[]
	in
	try_peg 0;;
let rec to_def_arr llist =
	(match llist with
		| [] -> []
		| h::t -> (Deferred.return h) :: (to_def_arr t)
	)


let solve rows =
	let () = printf "Solving with %d\n" rows in
	let peg_num = rows * (rows + 1) / 2 in
	let states =
		to_def_arr(Array.to_list (Array.init peg_num (fun i -> { pegs = Array.init peg_num (fun j -> if i == j then 0 else 1); history = [{ move = Start; idx = i }]})))
	in
	let rec solve_state states =
		match states with
			| [] -> raise (Failure "This should never happen!")
			| state :: states' -> Deferred.bind state
			(fun s ->
				let next_states = get_next_states s in
				if List.length next_states == 0 then
					Deferred.return s
				else
					solve_state (List.rev_append (List.rev states') next_states))
	in
	solve_state states;;

let print_move move =
	match move with
		| Start -> "Start"
		| Right -> "right"
		| Left -> "left"
		| Up_Right -> "up-right"
		| Up_Left -> "up-left"
		| Down_Right -> "down-right"
		| Down_Left -> "down-left";;

let print_idx idx =
	let (col, row) = to_col_row idx in
	sprintf "(%d, %d)" row col;;

let rec print_history history =
		match history with
			| [] -> ()
			| record :: history' -> let () = printf "%s %s\n" (print_idx record.idx) (print_move record.move) in
		print_history history';;

let () =
	let solution = solve (int_of_string Sys.argv.(1)) in
	Deferred.upon solution
		(fun s -> print_history (List.rev s.history);(shutdown 0));
	ignore(Scheduler.go());