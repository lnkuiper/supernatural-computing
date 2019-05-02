package algorithms;

public class KNPAnt implements Comparable<KNPAnt>{

    public boolean[] z;
    public double profit;
    public double weight;

    public KNPAnt(int numOfItems) {
        this.z = new boolean[numOfItems];
        this.profit = 0;
        this.weight = 0;
    }

    public void step(int nextItem, double profit, double weight) {
        // Move to next city and update values
        z[nextItem] = true;
        this.profit += profit;
        this.weight += weight;
    }

    public boolean[] getKnapsack() {
        return z;
    }

    @Override
    public int compareTo(KNPAnt otherAnt) {
        return -Double.compare(this.profit, otherAnt.profit);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof KNPAnt)) {
            return false;
        }
        else {
            KNPAnt otherAnt = (KNPAnt) o;
            return this.compareTo(otherAnt) == 0;
        }
    }
}
