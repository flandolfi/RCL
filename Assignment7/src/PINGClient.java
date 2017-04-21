// Francesco Landolfi
// Matr. 444151
// fran.landofli@gmail.com

import java.io.IOException;
import java.net.*;
import java.util.Arrays;
import java.util.Date;

// PINGClient class
public class PINGClient {
    // Main
    public static void main(String[] args) {
        // In case of exception, 'err' represent the index of wrong argument
        int err = 0;

        try {
            // Check arguments
            // If the retrieving of the first argument raises an exception, then the variable 'err'
            // will be '0' (i.e. the index of the first argument); else, 'err' will be set to '1'.
            InetAddress address = InetAddress.getByName(args[0]); err++;
            Integer port = new Integer(args[1]);

            // Create the socket
            try (DatagramSocket client = new DatagramSocket()) {
                long min = 0, max = 0, delay, timestamp;
                double avg = 0;
                int received = 0, sent;
                byte[] bMessage;
                String message;
                SocketAddress server = new InetSocketAddress(address, port);
                client.setSoTimeout(2000); // Timeout of 2 seconds for each PING request

                // Send 10 PING requests
                for (sent = 0; sent < 10; sent++) {
                    timestamp = System.currentTimeMillis();
                    message = "PING " + sent + " " + new Date(timestamp).toInstant();
                    bMessage = message.getBytes();
                    DatagramPacket request = new DatagramPacket(bMessage, bMessage.length, server);
                    DatagramPacket response = new DatagramPacket(new byte[bMessage.length], bMessage.length);

                    try {
                        // Send a PING packet; If there is no response after 2s, throws
                        // a SocketTimeoutException
                        client.send(request);
                        client.receive(response);
                        delay = System.currentTimeMillis() - timestamp;

                        // If the PING respond does not match the request, throw an exception
                        if (!Arrays.equals(request.getData(), response.getData()))
                            throw new IOException("Request and response arrays don't match");

                        System.out.println(message + " RTT " + delay + "ms");

                        // For every received packet, update general statistics
                        if (received == 0 || delay < min)
                            min = delay;

                        if (received == 0 || delay > max)
                            max = delay;

                        avg += delay;
                        received++;
                    } catch (SocketTimeoutException e) {
                        System.out.println(message + " *");
                    } catch (IOException e) {
                        System.err.println("Error in communication: " + e.getMessage());
                    }
                }

                avg = avg/received;

                // Show PING statistics
                System.out.println("\n---- PING Statistics ----");
                System.out.println(sent + " packets transmitted, " + received + " received, " +
                        (int) (100 - ((float) received / (float) sent) * 100) + "% packet loss");
                System.out.printf("RTT (ms) min/avg/max = %d/%.2f/%d\n", min, avg, max);
            } catch (IOException e) {
                System.err.println("Error with socket: " + e.getMessage());
            }
        } catch (ArrayIndexOutOfBoundsException | UnknownHostException | NumberFormatException e) {
            // Possible wrong argument exceptions
            System.err.println("ERR -arg " + err);
        }
    }
}
