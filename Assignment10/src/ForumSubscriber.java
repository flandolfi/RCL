// Francesco Landolfi
// Matr. 444151
// fran.landofli@gmail.com

import java.rmi.RemoteException;
import java.rmi.server.RemoteObject;

// ForumSubscriber class - Implements Subscriber stub
public class ForumSubscriber extends RemoteObject implements Subscriber {
    @Override
    public void sendNotification(String message) throws RemoteException {
        // Just print the message about a certain topic (after subscription)
        System.out.println(message);
    }
}
