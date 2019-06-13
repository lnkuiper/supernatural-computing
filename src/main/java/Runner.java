import algorithms.*;
//import algorithms.KNPRunner;
import model.Solution;
import model.TravelingThiefProblem;
import util.Linspace;

import java.io.*;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

class Runner {

    static final ClassLoader LOADER = Runner.class.getClassLoader();

    public static void main(String[] args) throws ClassNotFoundException, ExecutionException, InterruptedException, IOException {

        HashMap<String, Double> durationMap = new HashMap<>();
        HashMap<String, Double> profitMap = new HashMap<>();
        HashMap<String, Double> nadirMap = new HashMap<>();

        durationMap.put("a280", 2613.);
        durationMap.put("fnl4461", 185359.);
        durationMap.put("pla33810", 66048945.);

        profitMap.put("a280-n279", 42036.);
        profitMap.put("a280-n1395", 489194.);
        profitMap.put("a280-n2790", 1375443.);
        profitMap.put("fnl4461-n4460", 645150.);
        profitMap.put("fnl4461-n22300", 7827881.);
        profitMap.put("fnl4461-n44600", 22136989.);
        profitMap.put("pla33810-n33809", 4860715.);
        profitMap.put("pla33810-n169045", 59472432.);
        profitMap.put("pla33810-n338090", 168033267.);

        nadirMap.put("a280-n1395", 6769.05189);
        nadirMap.put("a280-n279", 7855.99276);
        nadirMap.put("a280-n2790", 6645.5851);
        nadirMap.put("fnl4461-n22300", 458389.45526);
        nadirMap.put("fnl4461-n4460", 461247.53136);
        nadirMap.put("fnl4461-n44600", 459900.52051);
        nadirMap.put("pla33810-n169045", 169415148.);
        nadirMap.put("pla33810-n33809", 168432301.);
        nadirMap.put("pla33810-n338090", 169605428.);

        List<String> instanceToRun = Arrays.asList("a280-n279");
//        List<String> instanceToRun = Arrays.asList("a280-n1395");
//        List<String> instanceToRun = Arrays.asList("a280-n2790");
//        List<String> instanceToRun = Arrays.asList("fnl4461-n4460");
//        List<String> instanceToRun = Arrays.asList("fnl4461_n22300");
//        List<String> instanceToRun = Arrays.asList("fnl4461_n44600");
//        List<String> instanceToRun = Arrays.asList("pla33810_n33809");
//        List<String> instanceToRun = Arrays.asList("pla33810_n169045");
//        List<String> instanceToRun = Arrays.asList("pla33810_n338090");
        //List<String> instanceToRun = Competition.INSTANCES;

        boolean runTSP = true;
        boolean runKNP = true;

        for (String instance : instanceToRun) {

            // readProblem the problem from the file
            String fname = String.format("resources/%s.txt", instance);
            InputStream is = LOADER.getResourceAsStream(fname);
            System.out.println(instance);
            TravelingThiefProblem problem = Util.readProblem(is);
            problem.name = instance;
            problem.initialize();

            problem.idealDuration = durationMap.get(instance.split("-")[0]) * 1.;
            problem.idealProfit = profitMap.get(instance);
            problem.nadirPoint = nadirMap.get(instance) * 1.05;

            // number of solutions that will be finally necessary for submission - not used here
            int numOfSolutions = Competition.numberOfSolutions(problem);

            if(runTSP){
                System.out.println("Starting TSP on problem " + instance);
                // TSP testing
                TSPRunner allColonies = new TSPRunner();
                List<List<Integer>> tours = allColonies.computeTours(problem);
                FileOutputStream fos = new FileOutputStream("savedTours/" + problem.name.split("-")[0] + ".obj");
                ObjectOutputStream oos = new ObjectOutputStream(fos);
                oos.writeObject(tours);
                oos.close();
                fos.close();
            }
            if(!runKNP) {
                System.exit(0);
            }
            System.out.println("Starting KNP on problem " + instance);
            // Actual submission stuff
            Algorithm algorithm = new IndependentSubproblemAlgorithm(numOfSolutions);
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
        System.exit(0);
    }

}
