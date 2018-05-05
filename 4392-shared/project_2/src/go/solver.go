package main

import (
    "os"
    "fmt"
    "strconv"
)

type Move struct {
    name string
    move func(x, y int) (int, int)
}

var moves = []Move {
    { "right", func(x, y int) (int, int) { return x + 1, y } },
    { "left", func(x, y int) (int, int) { return x - 1, y } },
    { "up-right", func(x, y int) (int, int) { return x, y - 1 } },
    { "up-left", func(x, y int) (int, int) { return x - 1, y - 1 } },
    { "down-right", func(x, y int) (int, int) { return x + 1, y + 1 } },
    { "down-left", func(x, y int) (int, int) { return x, y + 1 } },
}

func to_col_row(idx int) (int, int) {
    row := 0
    for idx > row {
        row += 1
        idx -= row
    }
    return idx, row
}

func to_idx(x, y int) int {
    for y > 0 {
        x += y
        y -= 1
    }

    return x
}

type BoardMove struct {
    peg int
    move *Move
}

type BoardState struct {
    pegs []int
    history []BoardMove
}

func make_move(state *BoardState, idx int, move *Move) *BoardState {
    is_oob := func(x, y int) bool {
        return x < 0 || y < 0 || to_idx(x, y) >= len(state.pegs) || x > y
    }

    pos_x, pos_y := to_col_row(idx)
    
    next_x, next_y := move.move(pos_x, pos_y)
    next_idx := to_idx(next_x, next_y)

    jump_x, jump_y := move.move(next_x, next_y)
    jump_idx := to_idx(jump_x, jump_y)

    if is_oob(pos_x, pos_y) || is_oob(next_x, next_y) || is_oob(jump_x, jump_y) || state.pegs[idx] == 0 || state.pegs[next_idx] == 0 || state.pegs[jump_idx] == 1 {
        return nil
    }

    new_pegs := make([]int, len(state.pegs))
    copy(new_pegs, state.pegs)
    new_pegs[idx] = 0
    new_pegs[next_idx] = 0
    new_pegs[jump_idx] = 1

    new_history := append(state.history, BoardMove{idx, move})

    return &BoardState{new_pegs, new_history}
}

func get_next_states(state *BoardState) []*BoardState {
    var states = make([]*BoardState, 0)

    for j := range moves {
        for i := range state.pegs {
            if next_state := make_move(state, i, &moves[j]); next_state != nil {
                states = append(states, next_state)
            }
        }
    }

    return states
}

func solve_start(start_state *BoardState, completed chan *BoardState, found chan int) {
    states := []*BoardState{start_state}

    var best_depth int = -1;

    for len(states) > 0 {
        select {
            case d := <- found:
                best_depth = d
                break;
            default:
                state := states[0]

                if best_depth != -1 && len(state.history) >= best_depth {
                    completed <- nil
                    return
                }

                next_moves := get_next_states(state)
                if len(next_moves) == 0 {
                    completed <- state
                    return
                }

                states = append(states[1:], next_moves...)
        }
    }

    completed <- nil
}

func multi_solve(n int) *BoardState {
    peg_num := (n * (n + 1)) / 2

    found_chans := make([]chan int, peg_num)
    completed := make(chan *BoardState, peg_num)

    for i := 0; i < peg_num; i++ {
        state := &BoardState{}
        state.pegs = make([]int, peg_num)
        for j := range state.pegs {
            if i == j {
                state.pegs[j] = 0
            } else {
                state.pegs[j] = 1
            }
        }

        state.history = append(state.history, BoardMove{i, nil})

        found_chans[i] = make(chan int, peg_num)
        go solve_start(state, completed, found_chans[i])
    }

    var best_solution *BoardState = nil

    for i := 0; i < peg_num; i++ {
        solution := <- completed

        if solution != nil && (best_solution == nil || len(best_solution.history) > len(solution.history)) {
            best_solution = solution

            for _, found := range found_chans {
                found <- len(best_solution.history)
            }
        }
    }

    return best_solution
}

func main() {
    n, _ := strconv.Atoi(os.Args[1])
    
    fmt.Println("Solving with", n)

    solution := multi_solve(n)

    for _, move := range solution.history {
        x, y := to_col_row(move.peg)
        if move.move == nil {
            fmt.Printf("(%d, %d) Start\n", y, x)
        } else {
            fmt.Printf("(%d, %d) %s\n", y, x, move.move.name)
        }
    }
}
