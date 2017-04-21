// Francesco Landolfi
// Matr. 444151
// fran.landolfi@gmail.com

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

// Pseudo-type
class Matrix extends ArrayList<ArrayList<Double>> {}

public class Main {
    // Read matrix from file
    private static Matrix readMatrix(String path) {
        Matrix mat = new Matrix();
        BufferedReader buf;
        String line;
        String[] tokens;

        System.out.println(Thread.currentThread() + ": Reading '" + path + "'...");

        try {
            buf = new BufferedReader(new FileReader(path));

            while ((line = buf.readLine()) != null) {
                ArrayList<Double> row = new ArrayList<>();

                tokens = line.split(" ");

                for (String token: tokens)
                    row.add(new Double(token));

                mat.add(row);
            }

            buf.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println(Thread.currentThread() + ": Done");
        return mat;
    }

    // Main
    public static void main(String[] args) {
        // Read two matrices from file
        Matrix m1, m2;
        m1 = readMatrix("matrix1.txt");
        m2 = readMatrix("matrix2.txt");

        try {
            // Create runnables
            MultiplicationTask task1 = new MultiplicationTask(m1,m2);
            AdditionTask task2 = new AdditionTask(m1,m2);

            // Start threads
            (new Thread(task1)).start();
            (new Thread(task2)).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
