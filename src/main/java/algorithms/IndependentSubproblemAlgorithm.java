package algorithms;

import model.NonDominatedSet;
import model.Solution;
import model.TravelingThiefProblem;
import util.Linspace;
import util.Logspace;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class IndependentSubproblemAlgorithm implements Algorithm {

    private int numberOfTrials;

    public IndependentSubproblemAlgorithm (int numberOfTrials) {
        this.numberOfTrials = numberOfTrials;
    }

    @Override
    public List<Solution> solve(TravelingThiefProblem problem) throws ExecutionException, InterruptedException {

        NonDominatedSet nds = new NonDominatedSet();

        double start = problem.idealDuration / problem.nadirPoint;
        double end = 1.0;
        int steps = (int) (2 * numberOfTrials);
        Logspace counter = new Logspace(start, end, steps, 1000);
        int i = 0;
        while (counter.hasNext()) {
            double c = counter.next();
            KNPRunner runner = new KNPRunner(c);
            KNPAnt bestAnt = runner.computePackingPlan(problem);
            List<Boolean> z = new ArrayList<>(problem.numOfItems);
            for (int j = 0; j < problem.numOfItems; j++) {
                z.add(bestAnt.z[j]);
            }
            Solution s = problem.evaluate(bestAnt.pi, z, true);
            nds.add(s);
            System.out.println(i + ":" + nds.entries.size() + " " + c + ", TIME: " + bestAnt.tourTime + ", MAXTIME: " + (c * (problem.nadirPoint - problem.idealDuration) + problem.idealDuration) + ", PROFIT: " + bestAnt.profit + ", WEIGHT: " + bestAnt.weight);
            i++;
        }
        System.out.println("Before: " + nds.entries.size());
        List<Solution> bestSolutions = nds.getBestSolutions(numberOfTrials);
        System.out.println("After: " + bestSolutions.size());
        return bestSolutions;
    }
}
