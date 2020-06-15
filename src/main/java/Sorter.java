public class Sorter implements Runnable {
    private int[] a;
    private int threadCount;

    public Sorter(int[] a, int threadCount) {
        this.a = a;
        this.threadCount = threadCount;
    }

    public void run() {
        MergeSort.parallelMergeSort(a, threadCount);
    }
}
