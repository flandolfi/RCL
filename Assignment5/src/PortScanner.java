// Francesco Landolfi
// Matr. 444151
// fran.landolfi@gmail.com

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class PortScanner {
    public static void main(String[] args) {
        // Ports lower & upper bounds;
        // Change the constants below for a narrower range
        final int RANGE_L = 0;
        final int RANGE_H = 65535;

        // Steps number of the progress bar
        final int STEPS = 20;

        try {
            // Get the address passed by argument
            InetAddress address = InetAddress.getByName(args[0]);

            for (int port = RANGE_L; port <= RANGE_H; port++) {
                double percent = (port - RANGE_L)/(double) (RANGE_H - RANGE_L);

                System.out.print("\rProgress: ");

                // Print a progress bar
                for (int i = 0; i < STEPS; i++) {
                    if(i/(double) STEPS <= percent)
                        System.out.print("▓");
                    else
                        System.out.print("░");
                }

                System.out.print(" " + (int) (100*percent) + "%; Scanning port " + port + "...");

                // Create a new socket and try to connect to address:port;
                // if an error occurs, ignore it; otherwise print the port number.
                try (Socket socket = new Socket()) {
                    InetSocketAddress sockAddr = new InetSocketAddress(address, port);
                    socket.connect(sockAddr, 50);
                    System.out.println("\rPort " + socket.getPort() + " on "
                            + socket.getInetAddress() + " is open");
                } catch (IOException e) {
                    // Ignore
                }
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            System.err.println("An hostname or an address must be passed as argument!");
        } catch (UnknownHostException e) {
            System.err.println("No such host: " + args[0]);
        }
    }
}
