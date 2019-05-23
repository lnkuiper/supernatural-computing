package algorithms;

import model.Solution;
import model.TravelingThiefProblem;

import java.util.List;
import java.util.concurrent.ExecutionException;

public interface Algorithm {

    /**
     * This method should be overriden by your algorithm to computeTours the problem
     * @param problem traveling thief problem instance
     * @return A non-dominated set of solutions
     */
    List<Solution> solve(TravelingThiefProblem problem) throws ExecutionException, InterruptedException;

}
