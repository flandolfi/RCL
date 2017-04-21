// Francesco Landolfi
// Matr. 444151
// fran.landolfi@gmail.com

// Addition thread
public class AdditionTask implements Runnable {
    private Matrix mLeft, mRight;

    public AdditionTask(Matrix left, Matrix right) throws IllegalArgumentException {
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

        if (lCols != rCols || lRows != rRows)
            throw new IllegalArgumentException("Left and right matrices must have the same dimensions");
    }

    @Override
    public void run() {
        String out = "";
        System.out.println(Thread.currentThread() + ": Just started");

        // Implement matrix addition
        for (int i = 0; i < mLeft.size(); i++) {
            for (int j = 0; j < mLeft.get(i).size(); j++)
                out += (mLeft.get(i).get(j) + mRight.get(i).get(j)) + " ";

            out += "\n";
        }

        System.out.println(Thread.currentThread() + ": Addition result:\n\n" + out);
        System.out.println(Thread.currentThread() + ": Done");
    }
}
