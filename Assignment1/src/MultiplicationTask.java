// Francesco Landolfi
// Matr. 444151
// fran.landolfi@gmail.com

// Multiplication thread
public class MultiplicationTask implements Runnable {
    private Matrix mLeft, mRight;

    public MultiplicationTask(Matrix left, Matrix right) throws IllegalArgumentException {
        int lRows, lCols, rRows, rCols;

        mLeft = left;
        mRight = right;

        // Check matrices' integrity
        lRows = mLeft.size();
        rRows = mRight.size();

        if (lRows == 0 || rRows == 0)
            throw new IllegalArgumentException("Matrix dimensions cannot be 0");

        lCols = mLeft.get(0).size();
        rCols = mRight.get(0).size();

        if (lCols == 0 || rCols == 0)
            throw new IllegalArgumentException("Matrix dimensions cannot be 0");

        for (int i = 1; i < lRows; i++)
            if(mLeft.get(i).size() != lCols)
                throw new IllegalArgumentException("Non-regular matrix");

        for (int i = 1; i < rRows; i++)
            if(mRight.get(i).size() != rCols)
                throw new IllegalArgumentException("Non-regular matrix");

        if (lCols != rRows)
            throw new IllegalArgumentException("The number of columns of the left matrix must equal the number of rows of the right matrix.");
    }

    @Override
    public void run() {
        Double res;
        String out = "";

        System.out.println(Thread.currentThread() + ": Just started");

        // Implement matrix product
        for (int i = 0; i < mLeft.size(); i++) {
            for (int j = 0; j < mRight.get(i).size(); j++) {
                res = 0.;

                for (int k = 0; k < mLeft.get(i).size(); k++) {
                    res += mLeft.get(i).get(k)*mRight.get(k).get(j);
                }

                out += res + " ";
            }

            out += "\n";
        }

        System.out.println(Thread.currentThread() + ": Multiplication result:\n\n" + out);
        System.out.println(Thread.currentThread() + ": Done");
    }
}
