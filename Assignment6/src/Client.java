// Francesco Landolfi
// Matr. 444151
// fran.landofli@gmail.com

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

// Client class
public class Client {
    public static void main(String[] args) {
        System.out.println("Connecting to server...");

        // Make a connection to the server
        try(Socket socket = new Socket(InetAddress.getLocalHost(), 1500);
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            BufferedReader localIn = new BufferedReader(new InputStreamReader(System.in)))
        {
            String option;
            ArrayList<Product> result;

            System.out.println("Connected.");
            System.out.println("Insert a product to query server, 'exit' to quit.");

            // Until the typed word(s) is/are not 'exit', ask the server for a sorted list of
            // products on every OnlineShop matching that word(s)
            while(!(option = localIn.readLine()).equals("exit")){
                System.out.println("Asking for \'" + option + "\"...");
                out.writeObject(option);
                out.flush();
                result = (ArrayList<Product>) in.readObject();
                System.out.println("Received " + result.size() + " products matching \""
                        + option + "\"");

                for (Product product: result){
                    System.out.println(product);
                }

                System.out.println("Insert a product to query server, 'exit' to quit.");
            }
        } catch (UnknownHostException e) {
            System.err.println("Unknown host");
        } catch (IOException e) {
            System.err.println("Error: "+ e.getMessage());
        } catch (ClassNotFoundException e) {
            System.err.println("Class not found: " + e.getMessage());
        }
    }
}
