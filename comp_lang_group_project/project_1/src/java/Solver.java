import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Solver is just a class to house the main method (see main).
*/
public class Solver {
    /**
     * main will solve for a worst solution to the triangle-peg problem and
     * print a solution to stdout.
     * @param args: The first element will be interpreted as the board size.
     *              All other elements of args will be ignored.
    */
    public static void main(String[] args) {
        int n;
        if (args.length < 1) {
            System.out.println("Must provide argument N (position 0)");
            return;
        } else {
            n = Integer.parseInt(args[0]);
        }
        BoardState end = null;
        Set<BoardState> explored = new HashSet<>();
        Queue<BoardState> toExplore = BoardState.AllStarting(n);

        while(!toExplore.isEmpty()) {
            BoardState s = toExplore.remove();
            if (!explored.contains(s)) {
                explored.add(s);
                Set<BoardState> newStates = s.getNextStates();
                if (newStates.isEmpty()) {
                    // No moves are possible and we have found a worst solution.
                    end = s;
                    break;
                }
                for (BoardState ss : newStates) {
                    toExplore.add(ss);
                }
            }
        }
        System.out.println(end.getMoves());
    }
}

