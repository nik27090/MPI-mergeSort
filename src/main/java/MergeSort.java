import java.util.Arrays;
import java.util.Random;

public class MergeSort {
        private static final Random RAND = new Random(42); // random number
        // generator

        public static void main(String[] args) throws Throwable {
            int LENGTH = 1000; // initial length of array to sort
            int RUNS = 16; // how many times to grow by 2?

            for (int i = 1; i <= RUNS; i++) {
                // First time length is 1000 elements then *2 for 16 RUNS
                int[] a = createRandomArray(LENGTH);
                System.out.println();
                System.out.println("Current RUNS is " + i);
                System.out.println();
                // run the algorithm and time how long it takes

                long startTime1 = System.currentTimeMillis();
                parallelMergeSort(a);
                long endTime1 = System.currentTimeMillis();

                /*int nbRunning = 0;
                for (Thread t : Thread.getAllStackTraces().keySet()) {
                    if (t.getState()==Thread.State.RUNNABLE) nbRunning++;
                }
                System.out.println("Total thread are "+nbRunning);*/
                if (!isSorted(a)) {
                    throw new RuntimeException("not sorted afterward: "
                            + Arrays.toString(a));
                }

                System.out.printf("%10d elements  =>  %6d ms \n", LENGTH, endTime1
                        - startTime1);
                LENGTH *= 2; // double size of array for next time
                // Length will be twice the first time
            }
        }

        public static void parallelMergeSort(int[] a) {
            //int cores = Runtime.getRuntime().availableProcessors();
            int cores = 4;
            parallelMergeSort(a, cores);
        }

        public static void parallelMergeSort(int[] a, int threadCount) {
            if (threadCount <= 1) {
                mergeSort(a);
            } else if (a.length >= 2) {
                // split array in half
                int[] left = Arrays.copyOfRange(a, 0, a.length / 2);
                int[] right = Arrays.copyOfRange(a, a.length / 2, a.length);

                // sort the halves
                // mergeSort(left);
                // mergeSort(right);
                Thread lThread = new Thread(new Sorter(left, threadCount / 2));
                Thread rThread = new Thread(new Sorter(right, threadCount / 2));
                lThread.start();
                rThread.start();

                try {
                    lThread.join();
                    rThread.join();
                } catch (InterruptedException ie) {
                    ie.printStackTrace();
                }

                // merge them back together
                merge(left, right, a);
            }
        }

        // Combines the contents of sorted left/right arrays into output array a.
        // Assumes that left.length + right.length == a.length.
        public static void merge(int[] left, int[] right, int[] a) {
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

        // Arranges the elements of the given array into sorted order
        // using the "merge sort" algorithm, which splits the array in half,
        // recursively sorts the halves, then merges the sorted halves.
        // It is O(N log N) for all inputs.
        public static void mergeSort(int[] a) {
            if (a.length >= 2) {
                // split array in half
                int[] left = Arrays.copyOfRange(a, 0, a.length / 2);
                int[] right = Arrays.copyOfRange(a, a.length / 2, a.length);

                // sort the halves
                mergeSort(left);
                mergeSort(right);

                // merge them back together
                merge(left, right, a);
            }
        }


        // Swaps the values at the two given indexes in the given array.
        public static void swap(int[] a, int i, int j) {
            if (i != j) {
                int temp = a[i];
                a[i] = a[j];
                a[j] = temp;
            }
        }

        // Randomly rearranges the elements of the given array.
        public static void shuffle(int[] a) {
            for (int i = 0; i < a.length; i++) {
                // move element i to a random index in [i .. length-1]
                int randomIndex = (int) (Math.random() * a.length - i);
                swap(a, i, i + randomIndex);
            }
        }

        // Returns true if the given array is in sorted ascending order.
        public static boolean isSorted(int[] a) {
            for (int i = 0; i < a.length - 1; i++) {
                if (a[i] > a[i + 1]) {
                    return false;
                }
            }
            return true;
        }

        // Creates an array of the given length, fills it with random
        // non-negative integers, and returns it.
        public static int[] createRandomArray(int length) {
            int[] a = new int[length];
            for (int i = 0; i < a.length; i++) {
                a[i] = RAND.nextInt(1000000);
                // a[i] = RAND.nextInt(40);
            }
            return a;
        }
}
