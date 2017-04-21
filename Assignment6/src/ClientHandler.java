// Francesco Landolfi
// Matr. 444151
// fran.landofli@gmail.com

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

// ClientHandler class
public class ClientHandler implements Runnable {
    private Socket client;
    private String name;

    // Constructor
    public ClientHandler(Socket client) {
        this.client = client;
        this.name = "[Handler " + client.getInetAddress() + ":" + client.getLocalPort() + "] ";
    }

    @Override
    public void run() {
        System.out.println(name + "Handling client.");

        // Accept requests from the client and send back the results from every search
        try(ObjectInputStream in = new ObjectInputStream(client.getInputStream());
            ObjectOutputStream out = new ObjectOutputStream(client.getOutputStream())){
            while(true){
                System.out.println(name + "Waiting for request...");
                String query = (String) in.readObject();
                System.out.println(name + "Received request for \"" + query + "\".");
                out.writeObject(Server.search(query));
                out.flush();
            }
        } catch (IOException e) {
            System.err.println(name + "Client disconnected or some error occurred.");
        } catch (ClassNotFoundException e) {
            System.err.println(name + "Class not found: " + e.getMessage());
        } finally{
            try {
                client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
