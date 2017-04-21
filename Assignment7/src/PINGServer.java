// Francesco Landolfi
// Matr. 444151
// fran.landofli@gmail.com

import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// PINGServer class
public class PINGServer {
    static final int MAX_LENGTH = 512;
    static final int THREADS = 5;

    // Main
    public static void main(String[] args) {
        // In case of exception, 'err' represent the index of wrong argument
        int err = 0;

        try {
            // Check arguments
            // If the retrieving of the first argument raises an exception, then the variable 'err'
            // will be '0' (i.e. the index of the first argument); else, 'err' will be set to '1'.
            int port = new Integer(args[0]); err++;
            long seed = args.length > 1 ? new Integer(args[1]) : System.currentTimeMillis();
            ExecutorService es = Executors.newFixedThreadPool(THREADS);
            Random random = new Random(seed);

            // Create the server
            try (DatagramSocket server = new DatagramSocket(port)) {
                ArrayList<PINGServerTask> tasks = new ArrayList<>();

                System.out.println("Server started; Waiting for PING requests...");

                // Create and start the server threads
                for (int i = 0; i < THREADS; i++) {
                    tasks.add(new PINGServerTask(server, random));
                }

                es.invokeAll(tasks);
            } catch (SocketException e) {
                System.err.println("Error creating socket: " + e.getMessage());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
            // Possible wrong argument exceptions
            System.err.println("ERR -arg " + err);
        }
    }
}