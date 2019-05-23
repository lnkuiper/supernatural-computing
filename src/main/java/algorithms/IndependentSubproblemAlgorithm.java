package algorithms;

import model.NonDominatedSet;
import model.Solution;
import model.TravelingThiefProblem;
import util.Linspace;

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

        Linspace counter = new Linspace(0, 1, numberOfTrials * 3);
        while (counter.hasNext()) {
            double c = counter.next();
            System.out.println(c);
            KNPRunner runner = new KNPRunner(c);
            KNPAnt bestAnt = runner.computePackingPlan(problem);
            List<Boolean> z = new ArrayList<>(problem.numOfItems);
            for (int j = 0; j < problem.numOfItems; j++) {
                z.add(bestAnt.z[j]);
            }
            Solution s = problem.evaluate(bestAnt.pi, z, true);
            nds.add(s);
        }
        return nds.getBestSolutions(numberOfTrials);
    }
}
