// Francesco Landolfi
// Matr. 444151
// fran.landofli@gmail.com

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Random;
import java.util.concurrent.Callable;

// PINGServerTask class
public class PINGServerTask implements Callable<Integer> {
    private DatagramSocket server;
    private Random random;

    // Constructor
    public PINGServerTask(DatagramSocket socket, Random random) {
        this.server = socket;
        this.random = random;
    }

    @Override
    public Integer call() {
        DatagramPacket request = new DatagramPacket(new byte[PINGServer.MAX_LENGTH],
                PINGServer.MAX_LENGTH);

        try {
            // Will stop if interrupted
            while (!Thread.interrupted()) {
                server.receive(request);
                System.out.println("[" + request.getAddress() + ":" + request.getPort() +
                        "] Received: " + new String(request.getData(), 0,
                        request.getLength(), "UTF-8"));

                // If a packet is received, choose a number from 0 to 4. If the chosen number
                // is 0, then do not respond; else send a response packet with a delay of 0-1s
                if (random.nextInt(5) >= 1) {
                    int delay = random.nextInt(1000);
                    DatagramPacket response = new DatagramPacket(request.getData(),
                            request.getLength(), request.getSocketAddress());

                    Thread.sleep(delay);
                    server.send(response);
                    System.out.println("[" + request.getAddress() + ":" + request.getPort()
                            + "] PING delayed by " + delay + "ms.");
                } else {
                    System.out.println("[" + request.getAddress() + ":" +
                            request.getPort() + "] PING not sent.");
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        return 0;
    }
}
