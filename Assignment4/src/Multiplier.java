// Francesco Landolfi
// Matr. 444151
// fran.landolfi@gmail.com

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

// Pseudo-type
class Matrix extends ArrayList<ArrayList<Integer>> {}

// MatrixMultiplier class
public class Multiplier implements Callable<ArrayList<Integer>> {
    private static Matrix leftMatrix, rightMatrix;
    int row;

    public Multiplier(int row) {
        this.row = row;
    }

    // Set the matrices to be multiplied
    public static void setMatrices(Matrix leftMatrix, Matrix rightMatrix) {
        int lRows = leftMatrix.size();
        int rRows = rightMatrix.size();

        // Check matrices' integrity
        if (lRows == 0 || rRows == 0)
            throw new IllegalArgumentException("Matrix dimensions cannot be 0");

        int lCols = leftMatrix.get(0).size();
        int rCols = rightMatrix.get(0).size();

        if (lCols == 0 || rCols == 0)
            throw new IllegalArgumentException("Matrix dimensions cannot be 0");

        for (int i = 1; i < lRows; i++)
            if(leftMatrix.get(i).size() != lCols)
                throw new IllegalArgumentException("Non-regular matrix");

        for (int i = 1; i < rRows; i++)
            if(rightMatrix.get(i).size() != rCols)
                throw new IllegalArgumentException("Non-regular matrix");

        if (lCols != rRows)
            throw new IllegalArgumentException("The number of columns of the left matrix "
                    + "must equal the number of rows of the right matrix.");

        Multiplier.leftMatrix = leftMatrix;
        Multiplier.rightMatrix = rightMatrix;
    }

    @Override
    public ArrayList<Integer> call() throws Exception {
        ArrayList<Integer> result = new ArrayList<>();
        int element;

        // Compute the 'row'-th row of the result matrix
        for (int i = 0; i < rightMatrix.get(row).size(); i++) {
            element = 0;

            for (int j = 0; j < rightMatrix.size(); j++)
                element += leftMatrix.get(row).get(j) * rightMatrix.get(j).get(i);

            result.add(element);
        }

        return result;
    }

    // Main
    public static void main(String[] args) {
        final int N = 1000; // Rows/Columns per matrix
        Matrix leftMatrix = new Matrix();
        Matrix rightMatrix = new Matrix();
        Random rand = new Random(System.currentTimeMillis());
        ArrayList<Multiplier> tasks = new ArrayList<>();

        // Check the presence of the parameter
        if (args.length == 0)
            throw new IllegalArgumentException("The number of threads should "
                    + "be passed as parameter!");

        ExecutorService ex = Executors.newFixedThreadPool(new Integer(args[0]));

        try {
            // Fill the matrices
            for (int i = 0; i < N; i++) {
                ArrayList<Integer> rowL = new ArrayList<>();
                ArrayList<Integer> rowR = new ArrayList<>();

                for (int j = 0; j < N; j++) {
                    rowL.add(rand.nextInt(100));
                    rowR.add(rand.nextInt(100));
                }

                leftMatrix.add(rowL);
                rightMatrix.add(rowR);
            }

            Multiplier.setMatrices(leftMatrix, rightMatrix);

            for (int i = 0; i < N; i++)
                tasks.add(new Multiplier(i));

            List<Future<ArrayList<Integer>>> results = ex.invokeAll(tasks);
            Matrix outputMatrix = new Matrix();

            // Recompose the result matrix
            //
            // Note: This works because the method 'invokeAll()' returns a list
            // of Futures in the same order of its parameter. Quoting Javadoc:
            // "Returns: a list of Futures representing the tasks, in the same
            // sequential order as produced by the iterator for the given task
            // list, each of which has completed".
            for (Future<ArrayList<Integer>> res: results)
                outputMatrix.add(res.get());

            // Process result...

//            // Change 'N' constant and uncomment for debugging
//            // (tested with Assignment #1)
//            // -----------------------------------------------
//
//            System.out.println("Left Matrix:");
//
//            for (int i = 0; i < N; i++) {
//                for (int j = 0; j < N; j++) {
//                    System.out.print(leftMatrix.get(i).get(j) + " ");
//                }
//
//                System.out.println();
//            }
//
//            System.out.println("\nRight Matrix:");
//
//            for (int i = 0; i < N; i++) {
//                for (int j = 0; j < N; j++) {
//                    System.out.print(rightMatrix.get(i).get(j) + " ");
//                }
//
//                System.out.println();
//            }
//
//            System.out.println("\nResult Matrix:");
//
//            for (int i = 0; i < N; i++) {
//                for (int j = 0; j < N; j++) {
//                    System.out.print(outputMatrix.get(i).get(j) + " ");
//                }
//
//                System.out.println();
//            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            ex.shutdown();
        }
    }
}
