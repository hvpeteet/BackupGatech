import java.util.Collections;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Roi Atalla
 */
public class Solver {
	public static void main(String[] args) {
		long now = System.currentTimeMillis();
		
		int n;
		if(args.length < 1) {
			System.out.println("Must provide argument N (position 0)");
			return;
		} else {
			n = Integer.parseInt(args[0]);
		}
		
		BoardState state = new Solver(n).solve();
		
		long diff = System.currentTimeMillis() - now;
		System.out.printf("Took %d milliseconds\n", diff);
		System.out.println(state.getMoves());
	}
	
	private final int n;
	private LinkedBlockingQueue<BoardState> completed;
	private ExecutorService executor;
	private Set<BoardState> explored;
	private AtomicInteger solutionDepth;
	private AtomicInteger processedCount;
	
	public Solver(int n) {
		this.n = n;
		completed = new LinkedBlockingQueue<>();
		executor = Executors.newFixedThreadPool(4);
		explored = Collections.newSetFromMap(new ConcurrentHashMap<>());
		solutionDepth = new AtomicInteger(Integer.MAX_VALUE);
		processedCount = new AtomicInteger(0);
	}
	
	public BoardState solve() {
		BoardState solution = null;
		
		for(BoardState state : BoardState.AllStarting(n)) {
			executor.execute(new Worker(state));
		}
		
		executor.shutdown();
		
		while(!executor.isTerminated() || !completed.isEmpty()) {
			try {
				BoardState state = completed.poll();
				if(state == null) {
					Thread.sleep(1);
					continue;
				}
				
				if(solution == null || state.getMoveCount() < solution.getMoveCount()) {
					solution = state;
					solutionDepth.set(solution.getMoveCount());
				}
			}
			catch(Exception exc) {
				exc.printStackTrace();
			}
		}
		
		return solution;
	}
	
	private class Worker implements Runnable {
		private BoardState startingState;
		
		public Worker(BoardState startingState) {
			this.startingState = startingState;
		}
		
		@Override
		public void run() {
			Queue<BoardState> toProcess = new LinkedList<>();
			toProcess.add(startingState);
			
			BoardState lastState = startingState;
			while(solutionDepth.get() > lastState.getMoveCount() && !toProcess.isEmpty()) {
				BoardState state = toProcess.poll();
				if(!explored.contains(state)) {
					lastState = state;
					processedCount.incrementAndGet();
					
					explored.add(state);
					Set<BoardState> nextStates = state.getNextStates();
					if(nextStates.isEmpty()) {
						completed.add(state);
						return;
					}
					toProcess.addAll(nextStates);
				}
			}
		}
	}
}
