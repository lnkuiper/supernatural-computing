import algorithms.*;
import model.Solution;
import model.TravelingThiefProblem;

import java.io.*;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;

class Runner {


    static final ClassLoader LOADER = Runner.class.getClassLoader();

    public static void main(String[] args) throws ClassNotFoundException, ExecutionException, InterruptedException, IOException {

        List<String> instanceToRun = Arrays.asList("a280-n279");
//        List<String> instanceToRun = Arrays.asList("fnl4461-n4460");
//        List<String> instanceToRun = Arrays.asList("pla33810-n33809");
        //List<String> instanceToRun = Competition.INSTANCES;

        for (String instance : instanceToRun) {

            // readProblem the problem from the file
            String fname = String.format("resources/%s.txt", instance);
            InputStream is = LOADER.getResourceAsStream(fname);
            System.out.println(instance);
            TravelingThiefProblem problem = Util.readProblem(is);
            problem.name = instance;
            problem.initialize();

            // number of solutions that will be finally necessary for submission - not used here
            int numOfSolutions = Competition.numberOfSolutions(problem);

            // TODO: remove this test code eventually
            KNPRunner allColonies = new KNPRunner();
            boolean[] packingPlan = allColonies.computePackingPlan(problem);

//            TSPRunner allColonies = new TSPRunner();
//            List<List<Integer>> tours = allColonies.computeTours(problem);
//            FileOutputStream fos = new FileOutputStream("savedTours/" + problem.name.split("-")[0] + ".obj");
//            ObjectOutputStream oos = new ObjectOutputStream(fos);
//            oos.writeObject(tours);
//            oos.close();
//            fos.close();
            System.exit(0);

            // initialize your algorithm
            Algorithm algorithm = new RandomLocalSearch(100);
            //Algorithm algorithm = new ExhaustiveSearch();

            // use it to to computeTours the problem and return the non-dominated set
            List<Solution> nds = algorithm.solve(problem);

            // sort by travelDistance and printSolutions it
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