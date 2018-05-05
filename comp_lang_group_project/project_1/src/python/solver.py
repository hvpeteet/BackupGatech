from enum import Enum

class Move(Enum):
	right = ("right", lambda pos: (pos[0] + 1, pos[1]))
	left = ("left", lambda pos: (pos[0] - 1, pos[1]))
	up_right = ("up-right", lambda pos: (pos[0], pos[1] - 1))
	up_left = ("up-left", lambda pos: (pos[0] - 1, pos[1] - 1))
	down_right = ("down-right", lambda pos: (pos[0] + 1, pos[1] + 1))
	down_left = ("down-left", lambda pos: (pos[0], pos[1] + 1))

def get_col_row(idx):
	row = 0
	while idx > row:
		row += 1
		idx -= row
	return idx, row

def get_idx(col_row):
	idx = col_row[0]
	row = col_row[1]

	while row > 0:
		idx += row
		row -= 1

	return idx

class Peg:
	def __init__(self, col_row, filled):
		self.col = col_row[0]
		self.row = col_row[1]
		self.filled = filled

	def copy(peg):
		return Peg((peg.col, peg.row), peg.filled)

class BoardState:
	def __init__(self, rows, start, pegs = None, moves = []):
		self.rows = rows
		self.start = start
		self.moves = moves
		self.hash = None

		#print("Init BoardState with rows=" + str(self.rows) + 
		#	", start=" + str(get_col_row(self.start)) + ", idx=" + str(get_idx(get_col_row(self.start))) + ", moves=" + str(self.moves))

		if pegs is None:
			self.pegs = [Peg(get_col_row(i), i != start) for i, _ in enumerate([1 for i in range(1, rows + 1) for j in range(1, i + 1)])]
		else:
			self.pegs = [Peg.copy(peg) for peg in pegs]

	def get_all_starting_states(rows):
		boards = []
		holes = int(rows * (rows + 1) / 2)

		for start in range(holes):
			boards.append(BoardState(rows, start))

		return boards

	def __eq__(self, that):
		for i, peg in enumerate(self.pegs):
			if peg.filled != that.pegs[i].filled:
				return False
		return True

	def __hash__(self):
		if self.hash:
			return self.hash

		value = 0
		for i, peg in enumerate(self.pegs):
			value += (1 << i) * (1 if peg.filled else 0)

		self.hash = value
		return value

	def make_move(self, peg_idx, move):
		def is_oob(pos):
			return pos[0] < 0 or pos[1] < 0 or pos[0] >= self.rows or pos[1] >= self.rows or pos[0] > pos[1]

		pos = get_col_row(peg_idx)

		if is_oob(pos) or not self.pegs[peg_idx].filled:
			return None

		next = move.value[1](pos)
		if is_oob(next) or not self.pegs[get_idx(next)].filled:
			return None

		jump = move.value[1](next)
		if is_oob(jump) or self.pegs[get_idx(jump)].filled:
			return None

		board = BoardState(self.rows, self.start, self.pegs, self.moves + [(pos, move.value[0])])
		board.pegs[peg_idx].filled = False
		board.pegs[get_idx(next)].filled = False
		board.pegs[get_idx(jump)].filled = True

		return board

	def __repr__(self):
		string = repr(list(map(lambda peg: 1 if peg.filled else 0, self.pegs)))
		string += "\nstart: " + str(get_col_row(self.start))
		string += "\nhistory: " + str(self.moves)
		return string


def solve(n):
	print("Solving with %d" % n)

	states = BoardState.get_all_starting_states(n)
	explored = set()

	while len(states) > 0:
		state = states.pop(0)

		if state in explored:
			continue

		explored.add(state)

		found_move = False
		for i, _ in enumerate(state.pegs):
			for move in Move:
				next_state = state.make_move(i, move)
				if next_state:
					states.append(next_state)
					found_move = True

		if not found_move:
			return state

	return None

if __name__ == "__main__":
	import sys
	solution = solve(int(sys.argv[1]))
	start_pos = get_col_row(solution.start)
	print(str((start_pos[1], start_pos[0])) + " Start")
	for move in solution.moves:
		print(str((move[0][1], move[0][0])) + " " + str(move[1]))
