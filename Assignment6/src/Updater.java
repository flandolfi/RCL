// Francesco Landolfi
// Matr. 444151
// fran.landofli@gmail.com

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Comparator;

// Updater class
public class Updater implements Runnable {
    private ArrayList<Integer> ports;

    // Constructor
    public Updater(ArrayList<Integer> ports) {
        this.ports = ports;
    }

    @Override
    public void run() {
        try {

            // Every 24 seconds, update the general product list.
            while (true) {
                ArrayList<Product> list = new ArrayList<>();
                System.out.println("[Updater] Updating product list...");

                // For each port (passed by command line), create a connection to the OnlineShop
                // server at that port (on localhost) and get its product list
                for (int port: ports) {
                    try (Socket socket = new Socket(InetAddress.getLocalHost(), port);
                         ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
                        System.out.println("[Updater] Connected to " + socket.getInetAddress()
                                + ":" + socket.getPort());
                        list.addAll((ArrayList<Product>) in.readObject());
                        System.out.println("[Updater] Received list from " + socket.getInetAddress()
                                + ":" + socket.getPort());
                    } catch (UnknownHostException e) {
                        System.err.println("[Updater] Unknown host: " + e.getMessage());
                    } catch (IOException e) {
                        System.err.println("[Updater] Error: " + e.getMessage());
                    } catch (ClassNotFoundException e) {
                        System.err.println("[Updater] Class not found: " + e.getMessage());
                    }
                }

                // Sort the merged product list by price
                list.sort(new Comparator<Product>() {
                    @Override
                    public int compare(Product p1, Product p2) {
                        return (int) Math.signum(p1.getPrice() - p2.getPrice());
                    }
                });

                // Update the product list on this server, then sleep 24 seconds
                Server.setProducts(list);
                System.out.println("[Updater] List updated.");
                Thread.sleep(24000);
            }
        } catch (InterruptedException e) {
            System.out.println("[Updater] Interrupted.");
        }
    }
}
