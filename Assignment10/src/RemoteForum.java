// Francesco Landolfi
// Matr. 444151
// fran.landofli@gmail.com

import java.rmi.RemoteException;
import java.rmi.server.RemoteObject;
import java.util.ArrayList;
import java.util.Hashtable;

// RemoteForum class - Implements the Forum stub
public class RemoteForum extends RemoteObject implements Forum {
    private static final long serialVersionUID = 1L;

    // A list of subscribers (clients) for each topic
    private Hashtable<String, ArrayList<Subscriber>> subscribers;

    // A list of messages for each topic
    private Hashtable<String, ArrayList<String>> topics;

    // Constructor
    public RemoteForum() {
        subscribers = new Hashtable<>();
        topics = new Hashtable<>();
    }

    // Returns the list of topics
    @Override
    public ArrayList<String> getTopics() throws RemoteException {
        System.out.println("Topic List requested from client.");

        // Note that ArrayList is serializable, Set is not.
        return new ArrayList<>(topics.keySet());
    }

    // Add a new topic
    @Override
    public void addTopic(String name) throws RemoteException {
        if (topics.containsKey(name))
            throw new RemoteException("Topic already exists.");

        // If the topic does not exist, create a new (empty) list of
        // subscribers and message under that topic.
        topics.put(name, new ArrayList<>());
        subscribers.put(name, new ArrayList<>());
        System.out.println("New topic '" + name + "' added.");
    }

    // Returns the list of messages sent about a certain topic
    @Override
    public ArrayList<String> getMessages(String topicName) throws RemoteException {
        if (!topics.containsKey(topicName))
            throw new RemoteException("Topic not found.");

        System.out.println("Requested message list about '" + topicName + "'.");
        return topics.get(topicName);
    }

    // Post a new message about a certain topic
    @Override
    public void sendMessage(String topicName, String message) throws RemoteException {
        if (!topics.containsKey(topicName))
            throw new RemoteException("Topic not found.");

        System.out.println("New message posted in '" + topicName + "': " + message);
        topics.get(topicName).add(message);

        // Notify each user (client) subscribed at that topic
        for (Subscriber subscriber: subscribers.get(topicName)) {
            subscriber.sendNotification("New message in \"" + topicName + "\": " + message);
        }
    }

    // Subscribe a user (client) under a certain topic
    @Override
    public void subscribe(Subscriber user, String topicName) throws RemoteException {
        if (!topics.containsKey(topicName))
            throw new RemoteException("Topic not found.");

        System.out.println("New subscription to '" + topicName + "'.");
        subscribers.get(topicName).add(user);
    }
}
