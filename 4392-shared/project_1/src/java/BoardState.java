import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

/**
 * BoardState holds all the logic used to manage exploring board states.
 * Each BoardState object contains exactly one specific state of the board.
 * (Has a valid peg state for each board location).
*/
public class BoardState {

    private PegState[][] pegs;
    private String moves = "";

    /**
     * Creates a new board with all holes filled. Only used in other constructors for utility.
     * @param n The dimension of the board (n x n board).
    */
    public BoardState(int n) {
        pegs = new PegState[n][];
        for (int i = 0; i < n; i++) {
            pegs[i] = new PegState[i+1];
            for (int j = 0; j <= i; j++) {
                pegs[i][j] = PegState.FULL;
            }
        }
    }

    /**
     * Creates a BoardState that has exactly 1 peg missing.
     * @param n The dimension of the board (n x n board).
     * @param r The row of the hole that should be empty
     * @param c The column of the hole that should be empty.
    */
    public BoardState(int n, int r, int c) {
        this(n);
        pegs[r][c] = PegState.EMPTY;
        moves = r + " " + c;
    }

    /**
     * Creates a copy of a board state.
     * @param parent The board to make this new board a copy of.
    */
    public BoardState(BoardState parent) {
        int n = parent.pegs.length;
        pegs = new PegState[n][];
        for (int i = 0; i < n; i++) {
            pegs[i] = new PegState[i+1];
            for (int j = 0; j <= i; j++) {
                pegs[i][j] = parent.pegs[i][j];
            }
        }
    }

    /**
     * Creates a queue of all starting states for a n x n board.
     * @param n The dimension of the board (n x n board).
    */
    public static Queue<BoardState> AllStarting(int n) {
        Queue<BoardState> s = new LinkedList<BoardState>();
        for (int i = 0; i < n; i++) {
            for (int j = 0; j <= i; j++) {
                s.add(new BoardState(n, i, j));
            }
        }
        return s;
    }

    /**
     * Defines the transition from one board state to another.
     * @param parent The original board.
     * @param r The row of the peg to move.
     * @param c The column of the peg to move.
     * @param m The move to make.
     * @return If the move is valid this will return a new board, if the move
     *         is invalid it will return null.
    */
    public static BoardState Move(BoardState parent, int r, int c, Move m) {
        int target_r = r + 2 * m.r;
        int target_c = c + 2 * m.c;
        int l = parent.pegs.length;

        // bounds check
        if (target_r < 0 || target_r >= l || target_c < 0 || target_c > target_r) {
            return null;
        }

        // If the target hole is empty and the other 2 are full the make the move.
        if ((parent.pegs[target_r][target_c] == PegState.EMPTY) &&
            (parent.pegs[r + m.r][c + m.c] == PegState.FULL)) {
                BoardState n = new BoardState(parent);
                n.pegs[r][c] = PegState.EMPTY;
                n.pegs[r + m.r][c + m.c] = PegState.EMPTY;
                n.pegs[target_r][target_c] = PegState.FULL;
                n.moves = parent.moves + "\n" + r + " " + c + " " + m.s;
                return n;
        }
        return null;
    }

    /**
     * Tries all possible moves from a certain state and gets all possible successors.
     * @return All possible states reachable in 1 move from the parent.
    */
    public Set<BoardState> getNextStates() {
        Set<BoardState> s = new HashSet<>();
        for (int i = 0; i < this.pegs.length; i++) {
            for (int j = 0; j < this.pegs[i].length; j++) {
                if(this.pegs[i][j] == PegState.FULL) {
                    for (Move m : Move.values()) {
                        BoardState b = BoardState.Move(this, i, j, m);
                        if (b != null) {
                            s.add(b);
                        }
                    }
                }
            }
        }
        return s;
    }

    /**
     * Returns a string representation of the board using X for filled holes and O for empty holes.
    */
    public String toString() {
        String s = "";
        for (int i = 0; i < pegs.length; i++) {
            for (int j = 0; j < pegs[i].length; j++) {
                if (pegs[i][j] == PegState.EMPTY) {
                    s += "O";
                } else {
                    s += "X";
                }
            }
            s += "\n";
        }
        return s;
    }

    /**
     * Gets the equivalent state of the board if you rotated it.
     * Three rotations will return the original state.
    */
    public BoardState rotation() {
        int n = this.pegs.length;
        BoardState newB = new BoardState(n);
        for (int d = n - 1; d >= 0; d--) {
            int r = d;
            int c = r;
            int i = 0;
            while (r >= 0 && c >= 0) {
                newB.pegs[d][i] = this.pegs[r][c];
                r--;
                c--;
                i++;
            }
        }
        return newB;
    }

    /**
     * Two states are considered equal if any of their rotations are equal.
     * @param obj The object to compare.
     * @return If obj is equivalent to this object.
    */
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!BoardState.class.isAssignableFrom(obj.getClass())) {
            return false;
        }
        boolean b1 = this.toString().equals(obj.toString());
        BoardState r1 = this.rotation();
        b1 = b1 || r1.toString().equals(obj.toString());
        b1 = b1 || r1.rotation().toString().equals(obj.toString());
        return b1;
    }

    /**
     * Gets the hash code for a state, this code is the same for all rotations of a set state.
     * @return The hash code.
    */
    public int hashCode() {
        BoardState b1 = this;
        BoardState b2 = b1.rotation();
        BoardState b3 = b2.rotation();
        return b1.realHash() ^ b2.realHash() ^ b3.realHash();
    }

    /**
     * Helper for hashCode
     * hashes the string representation of this state.
    */
    private int realHash() {
        return this.toString().hashCode();
    }

    /**
     * Gets a string representation of all the moves taken to get here.
     * @return A string representation of all the moves taken to get here
     *         (in the format specified by the PDF).
    */
    public String getMoves() {
        return this.moves;
    }
}
