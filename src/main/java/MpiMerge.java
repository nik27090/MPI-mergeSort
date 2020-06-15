import mpi.MPI;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import static mpi.MPI.INT;

public class MpiMerge {
    private static final Random RAND = new Random(42);
    private static ArrayList<Double> times = new ArrayList<>();
    private static int count = 15;

    public static void main(String[] args) {
        for (int i = 0; i < count ; i++) {
            start(args);
        }
    }

    public static void start(String[] args) {
        MPI.Init(args);
        int globalArraySize = 30000000;
        int[] globalArray = new int[globalArraySize];

        int id = MPI.COMM_WORLD.Rank();
        int numProcs = MPI.COMM_WORLD.Size();
        int height = (int) lg(numProcs);

        double startTime1 = 0.0;
        //time
        if (id == 0) {
            startTime1 = MPI.Wtime();
        }
        //if process 0, fill globalArray
        if (id == 0) {
            globalArray = createRandomArray(globalArraySize);
            //System.out.println(id + " UNSORTED ARRAY " + Arrays.toString(globalArray));
        }

        //fill localArray
        int localArraySize = globalArraySize / numProcs;
        int[] localArray = new int[localArraySize];
        MPI.COMM_WORLD.Scatter(globalArray, 0, localArraySize, INT,
                localArray, 0, localArraySize, INT, 0);
        //System.out.println(id + " localArray " + Arrays.toString(localArray));

        //mergeSort
        if (id == 0) {
            globalArray = mergeSort(height, id, localArray, localArraySize, globalArray);
        } else {
            mergeSort(height, id, localArray, localArraySize, null);
        }

        if (id == 0) {
            double endTime1 =  MPI.Wtime();
            //System.out.println("Result: " + Arrays.toString(globalArray));
            times.add(endTime1-startTime1);
            System.out.println(endTime1-startTime1);
            if (times.size() == count) {
                time(numProcs);
            }
        }
        MPI.Finalize();
    }

    private static void time(int numProc) {
        System.out.println("result");
        double avg = 0.0;
        for (double time : times) {
            avg += time;
        }
        avg /= count;

        double d= 0;
        for (double time: times) {
            d += Math.pow(avg - time, 2);
        }
        d /= count;

        double maxError = 2.6778 * Math.pow(d/count,0.5);
        System.out.println("numProc = " + numProc + " avg = "+ avg + " d = " + d + " maxError = " + maxError);
    }

    private static int[] mergeSort(int height, int id, int[] localArray, int localArraySize, int[] globalArray) {
        int myHeight = 0;
        Arrays.sort(localArray);

        int[] half1 = localArray;
        int[] half2;
        int[] mergeResult;

        int parent;
        int rightChild;
        while (myHeight < height) {
            parent = (id & (~(1 << myHeight)));

            if (parent == id) { //left child
                rightChild = (id | (1 << myHeight));

                half2 = new int[localArraySize];
                MPI.COMM_WORLD.Recv(half2, 0, localArraySize, INT,
                        rightChild, 0);

                mergeResult = new int[localArraySize * 2];
                merge(half1, half2, mergeResult);
                half1 = mergeResult;
                localArraySize = localArraySize * 2;
                myHeight++;
            } else {
                MPI.COMM_WORLD.Send(half1, 0, localArraySize, INT, parent, 0);
                myHeight = height;
            }
        }
        if(id == 0) {
            globalArray = half1;
        }
        return globalArray;
    }

    private static void merge(int[] left, int[] right, int[] a) {
        int i1 = 0;
        int i2 = 0;
        for (int i = 0; i < a.length; i++) {
            if (i2 >= right.length || (i1 < left.length && left[i1] < right[i2])) {
                a[i] = left[i1];
                i1++;
            } else {
                a[i] = right[i2];
                i2++;
            }
        }
    }

    private static double lg(double x) {
        return Math.log(x)/Math.log(2.0);
    }

    private static int[] createRandomArray(int length) {
        int[] a = new int[length];
        for (int i = 0; i < a.length; i++) {
            a[i] = RAND.nextInt(1000000);
             //a[i] = RAND.nextInt(40);
        }
        return a;
    }
}
