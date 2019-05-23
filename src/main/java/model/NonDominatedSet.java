package model;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;


/**
 * This is an example implementation of a non-dominated set. It updates the set whenever new
 * solutions are added.
 *
 */
public class NonDominatedSet {

    //! entries of the non-dominated set
    public List<Solution> entries = new LinkedList<>();

    /**
     * Add a solution to the non-dominated set
     * @param s The solution to be added.
     * @return true if the solution was indeed added. Otherwise false.
     */
    public boolean add(Solution s) {

        boolean isAdded = true;

        for (Iterator<Solution> it = entries.iterator(); it.hasNext();) {
            Solution other = it.next();

            int rel = s.getRelation(other);

            // if dominated by or equal in design space
            if (rel == -1 || (rel == 0 && s.equalsInDesignSpace(other))) {
                isAdded = false;
                break;
            } else if (rel == 1) it.remove();

        }

        if (isAdded) entries.add(s);

        return isAdded;

    }

    private void normaliseSolutions(List<Solution> entries){
        double maxProfit = Double.NEGATIVE_INFINITY;
        double minTime = Double.POSITIVE_INFINITY;
        double maxTime = Double.NEGATIVE_INFINITY;
        for (Solution s: entries){
            if(s.profit > maxProfit){
                maxProfit = s.profit;
            }
            if(s.time > maxTime){
                maxTime = s.time;
            }
            if(s.time < minTime){
                minTime = s.time;
            }
        }

        for (Solution s: entries){
            s.profit /= maxProfit;
            s.time = (s.time-minTime)/(maxTime-minTime);
        }


    }
    public List<Solution> getBestSolutions(int numberOfSolutions){
        int excessSolutions = entries.size()-numberOfSolutions;
        for (int i = 0; i < excessSolutions; i++) {
            double leastArea = Double.POSITIVE_INFINITY;
            int leastIndex = -1;
            for (int j = 1; j < entries.size() - 1; j++) {
                double addedArea = Math.abs(entries.get(j-1).profit-entries.get(j).profit)*Math.abs(entries.get(j).time-entries.get(j+1).time);
                if(addedArea < leastArea){
                    leastArea  = addedArea;
                    leastIndex = j;
                }
            }
            entries.remove(leastIndex);

        }
        return entries;
    }


}
