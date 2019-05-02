package util;

import java.util.Arrays;

public class SymmetricArray {

    private float[] arr;

    public SymmetricArray(int dim, float initVal) {
        int triangleNum = triangle(dim - 1);
        this.arr = new float[triangleNum];
        Arrays.fill(arr, initVal);
    }

    private int triangle(int n) {
        return n * (n + 1) / 2;
    }

    private int getIndex(int i, int j) {
        if (j > i) {
            int temp = i;
            i = j;
            j = temp;
        }
        return triangle(i - 1) + j;
    }

    public float get(int i, int j) {
        if (i == j) {
            return 0;
        }
        return arr[getIndex(i, j)];
    }

    public void set(int i, int j, float val) {
        if (i != j) {
            arr[getIndex(i, j)] = val;
        }
    }
}
