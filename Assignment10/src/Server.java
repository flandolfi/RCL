// Francesco Landolfi
// Matr. 444151
// fran.landofli@gmail.com

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;


// Server class
public class Server {
    public static final int PORT = 2000; // Registry port

    // Main
    public static void main(String[] args) {
        try {
            Forum forum = (Forum) UnicastRemoteObject.exportObject(new RemoteForum(), 0);
            Registry registry = LocateRegistry.createRegistry(PORT);
            registry.rebind(Forum.OBJECT_NAME, forum);
            System.out.println("Server started.");
        } catch (RemoteException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}
