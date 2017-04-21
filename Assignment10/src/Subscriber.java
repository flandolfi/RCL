// Francesco Landolfi
// Matr. 444151
// fran.landofli@gmail.com

import java.rmi.Remote;
import java.rmi.RemoteException;

// Subscriber class STUB
public interface Subscriber extends Remote {
    public static final String OBJECT_NAME = "SUBSCRIBER";

    public void sendNotification(String message) throws RemoteException;
}
