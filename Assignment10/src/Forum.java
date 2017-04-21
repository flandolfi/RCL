// Francesco Landolfi
// Matr. 444151
// fran.landofli@gmail.com

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

// Forum class STUB
public interface Forum extends Remote {
    public static final String OBJECT_NAME = "FORUM";

    public ArrayList<String> getTopics() throws RemoteException;
    public void addTopic(String name) throws RemoteException;
    public ArrayList<String> getMessages(String topicName) throws RemoteException;
    public void sendMessage(String topicName, String message) throws RemoteException;
    public void subscribe(Subscriber user, String topicName) throws RemoteException;
}
