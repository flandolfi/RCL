// Francesco Landolfi
// Matr. 444151
// fran.landolfi@gmail.com

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class ConcurrentWordLookup implements Runnable {
    private static BufferedReader reader;
    private static String word;
    private static boolean found = false;
    private static int count = 0;
    private boolean first = false;
    private int currentLine;

    /* Two threads may found the same word at different lines. E.g.:
     *
     *      Thread-0 found a match at line 1
     *                  Thread-1 found a match at line 2
     *                  Thread-1 set 'found' to true
     *      Thread-0 set 'found' to true
     *      Thread-0 says "match found at line 1"
     *                  Thread-1 says "match found at line 2"
     *
     * Using this method, only ONE thread modifies the 'found' variable, and then
     * outputs the found match.
     *
     * The 'first' variable helps tracking the thread that found the first match. */
    private synchronized void setFound() {
        if(!found) {
            first = true;
            found = true;
        }
    }

    /* BufferedReader.readLine() is already thread-safe, but this method also keeps
     * the count of the read lines (globally) and, for each thread, stores the number
     * of the line that is currently analyzed. If a match is found, that number will
     * be printed on screen. */
    private synchronized String getLine() throws IOException {
        count++;
        currentLine = count;

        return reader.readLine();
    }

    public static void setReader(BufferedReader reader) {
        ConcurrentWordLookup.reader = reader;
    }

    public static BufferedReader getReader() {
        return reader;
    }

    public static void setWord(String word) {
        ConcurrentWordLookup.word = word;
    }

    @Override
    public void run() {
        String line;

        try {
            System.out.println(Thread.currentThread().getName() + ": Just started.");

            // If a match is not found, get a new line and search for a match
            while (!found && (line = getLine()) != null)
                if(line.contains(word))
                    setFound();

            /* If *this* thread found a match, print the line of the current match,
             * otherwise it's been either interrupted (a match was found by another thread)
             * or has reached the EOF.
             *
             * NOTE: A thread may print "No match found." even if another thread finds
             * a match! */
            if (first)
                System.out.println(Thread.currentThread().getName() + ": Found a match for '"
                        + word + "' at line " + currentLine);
            else if (found)
                System.out.println(Thread.currentThread().getName() + ": Interrupted.");
            else
                System.out.println(Thread.currentThread().getName() + ": No match found.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            ArrayList<Thread> threads = new ArrayList<>();

            // Check arguments
            if(args.length == 0)
                throw new IllegalArgumentException("A word must be passed as a command-line parameter.");

            // If multiple arguments are passed, use the first (ignore the others).
            ConcurrentWordLookup.setWord(args[0]);
            ConcurrentWordLookup.setReader(new BufferedReader(new FileReader("file.txt")));

            // Start 5 threads
            for (int i = 0; i < 5; i++) {
                ConcurrentWordLookup r = new ConcurrentWordLookup();
                Thread t = new Thread(r);
                t.start();
                threads.add(t);
            }

            // Wait for threads
            for (Thread t: threads)
                t.join();

            ConcurrentWordLookup.getReader().close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
