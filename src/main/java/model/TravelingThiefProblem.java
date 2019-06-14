package model;

import algorithms.KNPAnt;

import java.io.*;
import java.util.*;
import util.Vector;

/**
 * This class represents the main problem to be solved. It stores all variables which are necessary
 * to evaluate the objective function.
 *
 * Either define the problem by yourself or load it from a file.
 *
 */
public class TravelingThiefProblem {

    //! name of the problem - set if read from a file
    public String name = "unknown";

    //! number of cities
    public int numOfCities = -1;

    //! number of items
    public int numOfItems = -1;

    // ! minimal speed of the salesman
    public double minSpeed = -1;

    // ! maximal speed of the salesman
    public double maxSpeed = -1;

    // ! maximal weight of the knapsack
    public int maxWeight = -1;

    //! Renting Rate (not needed for multi-objective version)
    public double R = Double.POSITIVE_INFINITY;

    // ! coordinate where the salesman could visit cities
    public double[][] coordinates;

    //! corresponding city of each item
    public int[] cityOfItem;

    // ! the weight of each item
    public double[] weight;
    public double maxItemWeight = -1;

    // ! the profit of each item
    public double[] profit;
    public double maxItemProfit = -1;

    // ! used for faster evaluation
    private List<LinkedList<Integer>> itemsAtCity = null;

    // ! used for fitness normalization
    public float greedyDistance = 0;
    public List<Integer> greedyTour = new ArrayList<>();

    // ! best tours from TSP used for TTP
    public List<List<Integer>> bestTours;

    // ! estimated ideal point
    public double idealDuration;
    public double idealProfit;
    public double nadirPoint;

    /**
     * Initialize the problem by saving for each city the items to pick
     */
    public void initialize() throws IOException, ClassNotFoundException {

        // make the checks to avoid wrong parameters for the problem
        if (numOfCities == -1 || numOfItems == -1 || minSpeed == -1 || maxSpeed == -1|| maxWeight == -1
                || R == Double.POSITIVE_INFINITY)
            throw new RuntimeException("Error while loading problem. Some variables are not initialized");

        // initialize the itemsAtCity data structure
        this.itemsAtCity = new ArrayList<>(this.numOfCities);
        for (int i = 0; i < this.numOfCities; i++) {
            this.itemsAtCity.add(new LinkedList<>());
        }
        for (int i = 0; i < this.cityOfItem.length; i++) {
            this.itemsAtCity.get(this.cityOfItem[i]).add(i);
        }

        // Compute values for TSP pheromone initialization
        boolean[] visited = new boolean[numOfCities];
        int currentCity = 0;
        greedyTour.add(currentCity);
        for (int i = 0; i < numOfCities-1; i++) {
            visited[currentCity] = true;
            double bestDist = Double.POSITIVE_INFINITY;
            int bestCity = 0;
            for (int j = 0; j < numOfCities; j++) {
                double dist = euclideanDistance(currentCity, j);
                if(dist < bestDist && !visited[j]) {
                    bestDist = dist;
                    bestCity = j;
                }
            }
            greedyDistance += bestDist;
            currentCity = bestCity;
            greedyTour.add(currentCity);
        }
        greedyDistance += euclideanDistance(currentCity, 0);

        File toursFile = new File("savedTours/" + name.split("-")[0] + ".obj");
        if (toursFile.exists()) {
            FileInputStream fis = new FileInputStream("savedTours/" + name.split("-")[0] + ".obj");
            ObjectInputStream ois = new ObjectInputStream(fis);
            bestTours = (ArrayList<List<Integer>>) ois.readObject();
        }
        bestTours = bestTours.subList(0, 2);
        List<List<Integer>> reverseTours = new ArrayList<>();
        for (int i = 0; i < bestTours.size(); i++) {
            reverseTours.add(bestTours.get(i));
        }
        for (List<Integer> tour : reverseTours) {
            List<Integer> partToReverse = tour.subList(1, numOfCities - 1);
            Collections.reverse(partToReverse);
            bestTours.add(tour);
        }
        System.out.println("Tours loaded: " + bestTours.size());

        // Compute max item profit for normalization
        for (double ip : profit) {
            if (ip > maxItemProfit) {
                maxItemProfit = ip;
            }
        }

        // Compute max item weight for normalization
        for (double iw : weight) {
            if (iw > maxItemWeight) {
                maxItemWeight = iw;
            }
        }
    }
    public double getTourLength(List<Integer> tour){
        double distance = 0;
        for (int i = 0; i < numOfCities; i++) {
            distance += euclideanDistance(tour.get(i), tour.get((i+1)%numOfCities));
        }
        return distance;
    }


    public double speedFromWeight(double weight){
        return Math.max(minSpeed, maxSpeed - (weight/maxWeight)*(maxSpeed-minSpeed));
    }

    /**
     * See evaluate(pi,z,copy). Per default pi and z are not copied.
     */
    public Solution evaluate(List<Integer> pi, List<Boolean> z) {
        return evaluate(pi, z, false);
    }

    /**
     * The evaluation function of the problem to simulate the tour of the thief.
     * @param pi the tour
     * @param z the packing plan
     * @param copy if true the returned solution object has a copy of the tour and packing plan - otherwise
     *             just a reference. Be careful here, if you change the tour afterwards, the result will not match finally.
     *
     * @return A solution objective containing
     */
    public Solution evaluate(List<Integer> pi, List<Boolean> z, boolean copy) {

        if (pi.size() != this.numOfCities || z.size() != this.numOfItems) {
            throw new RuntimeException("Wrong input for traveling thief evaluation!");
        } else if(pi.get(0) != 0) {
            throw new RuntimeException("Thief must start at city 0!");
        }

        // the values that are evaluated in this function
        double time = 0;
        double profit = 0;

        // attributes in the beginning of the tour
        double weight = 0;

        // iterate over all possible cities
        for (int i = 0; i < this.numOfCities; i++) {

            // the city where the thief currently is
            int city = pi.get(i);

            // for each item index this city
            for (int j : this.itemsAtCity.get(city)) {

                // if the thief picks that item
                if (z.get(j)) {
                    // update the current weight and profit
                    weight += this.weight[j];
                    profit += this.profit[j];
                }

            }

            // if the maximum capacity constraint is reached
            if (weight > maxWeight) {
                time = Double.MAX_VALUE;
                profit = - Double.MAX_VALUE;
                break;
            }

            // update the speed accordingly
            double speed = this.maxSpeed - (weight / this.maxWeight) * (this.maxSpeed - this.minSpeed);

            // increase travelDistance by considering the speed - do not forget the way from the last city to the first!
            int next = pi.get((i + 1) % this.numOfCities);
            double distance = Math.ceil(euclideanDistance(city, next));

            time += distance / speed;

        }

        // create the final solution object
        Solution s = new Solution();
        if (copy) {
            s.pi = new ArrayList<>(pi);
            s.z = new ArrayList<>(z);
        } else {
            s.pi = pi;
            s.z = z;
        }
        s.time = time;
        s.profit = profit;
        s.singleObjective = profit - this.R * time;
        s.objectives =  Arrays.asList(time, -profit);

        return s;

    }

    public float euclideanDistance(int a, int b) {
        float distance = (float) Math.sqrt(Math.pow(this.coordinates[a][0] - this.coordinates[b][0], 2)
                        + Math.pow(this.coordinates[a][1] - this.coordinates[b][1], 2));
        if (distance == 0) {
            return (float) 0.000001;
        }
        return distance;
    }

    public List<Integer> getItemsAtCity(int c) {
        return itemsAtCity.get(c);
    }


    /**
     * This method verifies if the tour and packing plan is matching with the objective values saved in this object.
     * @throws RuntimeException
     */
    public void verify(Solution s) throws RuntimeException {
        Solution correct = this.evaluate(s.pi, s.z);
        if (s.time != correct.time || s.profit != correct.profit) {
            throw new RuntimeException("Pi and Z are not matching with the objectives values travelDistance and profit.");
        }
    }

    public double distanceToIdealPoint(KNPAnt ant) {
        double normalizedTime = (ant.tourTime - idealDuration) / (nadirPoint - idealDuration);
        double normalizedProfit = ant.profit / idealProfit;
        Vector vector = new Vector();
        return vector.pointDistance(0, 1, normalizedTime, normalizedProfit);
    }
}
