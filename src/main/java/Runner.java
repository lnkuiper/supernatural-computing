import algorithms.Algorithm;
import algorithms.TSPAntColony;
import algorithms.RandomLocalSearch;
import model.Solution;
import model.TravelingThiefProblem;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

class Runner {


    static final ClassLoader LOADER = Runner.class.getClassLoader();

    public static void main(String[] args) throws IOException {

        List<String> instanceToRun = Arrays.asList("a280-n279");
        //List<String> instanceToRun = Competition.INSTANCES;

        for (String instance : instanceToRun) {

            // readProblem the problem from the file
            String fname = String.format("resources/%s.txt", instance);
            InputStream is = LOADER.getResourceAsStream(fname);

            TravelingThiefProblem problem = Util.readProblem(is);
            problem.name = instance;
            System.out.println("Problem read.");

            // number of solutions that will be finally necessary for submission - not used here
            int numOfSolutions = Competition.numberOfSolutions(problem);

            // TODO: remove this test code
            double antFrac = 0.5;
            int numAnts = (int) (antFrac * problem.numOfCities);
            TSPAntColony AC = new TSPAntColony(problem,
                    10, 1000, numAnts,
                    15, 20, 0.1,
                    0.9, 0.0005, false);
            List<List<Integer>> bestTours = AC.computeTours();
            System.exit(0);

            // initialize your algorithm
            Algorithm algorithm = new RandomLocalSearch(100);
            //Algorithm algorithm = new ExhaustiveSearch();

            // use it to to computeTours the problem and return the non-dominated set
            List<Solution> nds = algorithm.solve(problem);

            // sort by time and printSolutions it
            nds.sort(Comparator.comparing(a -> a.time));

            System.out.println(nds.size());
            for(Solution s : nds) {
                System.out.println(s.time + " " + s.profit);
            }

            Util.printSolutions(nds, true);
            System.out.println(problem.name + " " + nds.size());

            File dir = new File("results");
            if (!dir.exists()) dir.mkdirs();
            Util.writeSolutions("results", Competition.TEAM_NAME, problem, nds);


        }



    }

}