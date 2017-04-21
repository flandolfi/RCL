// Francesco Landolfi
// Matr. 444151
// fran.landofli@gmail.com

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

// OnlineShop class (STUB)
public class OnlineShop {

    // Arguments from command line:
    // - 1st arg. is the name of the seller (will be shown on each product);
    // - 2nd arg. is the JSON file path, containing the product that will be
    //   sold from this server;
    // - 3rd arg. is the port that will be used from this server to accept
    //   connections.
    public static void main(String[] args) {
        try {
            ArrayList<Product> products = new ArrayList<>();
            String sellerName = args[0];
            String path = args[1];
            Integer port = new Integer(args[2]);

            // Parse the JSON file
            // Note: This should be done every accepted connections, but this is just
            // a stub (the data doesn't change!), so we parse the data only once.
            try (BufferedReader file = new BufferedReader(new InputStreamReader(
                    new FileInputStream(path)))) {
                JSONParser parser = new JSONParser();
                JSONObject JSONFile = (JSONObject) parser.parse(file);
                JSONArray JSONProducts = (JSONArray) JSONFile.get("products");

                for (Object product: JSONProducts) {
                    products.add(new Product(sellerName,
                            (String) ((JSONObject) product).get("manufacturer"),
                            (String) ((JSONObject) product).get("model"),
                            (long) ((JSONObject) product).get("price")));
                }

                file.close();

                // Create the server
                try (ServerSocket server = new ServerSocket()) {
                    server.bind(new InetSocketAddress(InetAddress.getLocalHost(), port));

                    while (true) {
                        System.out.println("Waiting for clients...");

                        // Accept connections and send the product list
                        try (Socket client = server.accept();
                             ObjectOutputStream out = new ObjectOutputStream(
                                     client.getOutputStream())) {
                            System.out.println("Client connected; Sending product list.");
                            out.writeObject(products);
                            out.flush();
                        } catch (IOException e) {
                            System.err.println("Client closed connection or some error appeared");
                        }
                    }
                }
            } catch (FileNotFoundException e) {
                System.err.println("\"" + path + "\" not found!");
            } catch (ParseException e) {
                System.err.println("Error parsing: " + e.getMessage());
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            System.err.println("Missing argument in command line!");
        }
    }
}
