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
            Conference conference = (Conference) UnicastRemoteObject
                    .exportObject(new ConferenceDays(), 0);
            Registry registry = LocateRegistry.createRegistry(PORT);
            registry.rebind(Conference.OBJECT_NAME, conference);
            System.out.println("Server ready");
        } catch (RemoteException e) {
            System.err.println("Server error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
