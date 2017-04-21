// Francesco Landolfi
// Matr. 444151
// fran.landolfi@gmail.com

public class Test {
    public static void main(String[] args) {
        long startTime, elapsedTime, avgTime, bestTime = 0;
        int bestThreads = 0;

        for (int i = 1; i < 10; i++) {
            System.out.println("Number of threads in the pool: " + i);
            System.out.println("--------------------------------");
            avgTime = 0;

            for (int j = 0; j < 3; j++) {
                System.out.print("Test " + j + ": ");
                startTime = System.currentTimeMillis();
                Multiplier.main(new String[]{ (new Integer(i)).toString() });
                elapsedTime = System.currentTimeMillis() - startTime;
                avgTime += elapsedTime;
                System.out.println(elapsedTime + "ms");
            }

            avgTime /= 3;
            System.out.println("\nAverage elapsed time: " + avgTime + "ms\n");

            if (i == 1 || avgTime < bestTime) {
                bestThreads = i;
                bestTime = avgTime;
            }
        }

        System.out.println("\nBest performance with "
                + bestThreads + " threads (" + bestTime + "ms)");
    }
}
