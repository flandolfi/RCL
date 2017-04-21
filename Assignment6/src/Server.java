// Francesco Landolfi
// Matr. 444151
// fran.landofli@gmail.com

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// Server class
public class Server {
    private static ArrayList<Product> products = new ArrayList<>();

    // Search a product from the list (thread safe)
    public static synchronized ArrayList<Product> search(String query) {
        ArrayList<Product> result = new ArrayList<>();
        String str = query.trim().toLowerCase();

        for (Product product: products){
            if (product.getManufacturer().trim().toLowerCase().contains(str) ||
                    product.getModel().trim().toLowerCase().contains(str) ||
                    product.getSeller().trim().toLowerCase().contains(str)) {
                result.add(product);
            }
        }

        return result;
    }

    // Update the list (thread safe)
    public static synchronized void setProducts(ArrayList<Product> products) {
        Server.products = products;
    }

    // Command line arguments:
    // - a list of port numbers (integers), representing each OnlineShop server
    //   (listening on localhost)
    public static void main(String[] args) {
        ArrayList<Integer> ports = new ArrayList<>();

        for (String arg: args) {
            ports.add(new Integer(arg));
        }

        // Create and launch the Updater thread
        Updater updater = new Updater(ports);
        Thread thread = new Thread(updater);
        ExecutorService es = Executors.newFixedThreadPool(20);

        thread.start();

        // Create the server and accept every new connection. Once a new connection
        // is created, the client i handled by a new thread.
        try (ServerSocket server = new ServerSocket(1500)) {
            while (true) {
                System.out.println("[Server] Waiting for clients...");
                Socket client = server.accept();
                System.out.println("[Server] Client arrived.");
                ClientHandler handler = new ClientHandler(client);
                es.submit(handler);
            }
        } catch (IOException e) {
            System.err.println("[Server] Error: " + e.getMessage());
        } finally {
            thread.interrupt();
            es.shutdown();
        }
    }
}
