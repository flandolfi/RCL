// Francesco Landolfi
// Matr. 444151
// fran.landofli@gmail.com

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.NoSuchObjectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

// Client class
public class Client {

    // Main
    public static void main(String[] args) {
        Subscriber user = new ForumSubscriber();

        try {
            Subscriber subscriber = (Subscriber) UnicastRemoteObject
                    .exportObject(user, 0);
            Forum forum = (Forum) LocateRegistry.getRegistry(Server.PORT).lookup(Forum.OBJECT_NAME);
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            String topic, op = "";

            // Ask for an operation until 'e' or 'E' is typed:
            //  - P: Print the topic list;
            //  - T: Show all messages about a certain topic (given by input);
            //  - A: Add a new topic (given by input);
            //  - M: Send a new message about a certain topic (with text and topic given by input);
            //  - S: Subscribe to a certain topic (given by input);
            //  - E: Exit and terminate this program.
            // For every operation, call the relative Forum method and then handle every
            // possible exception.
            do {
                try {
                    System.out.print("\nSelect an operation:\n" +
                            " - L: Print the topic list;\n" +
                            " - T: Show all messages of a topic;\n" +
                            " - A: Add a new topic;\n" +
                            " - M: Send a new message;\n" +
                            " - S: Subscribe;\n" +
                            " - E: Exit.\n Type [L|T|A|M|S|E]: ");
                    op = in.readLine().trim().toLowerCase();

                    // Parse the reply
                    if (op.equals("l")) { // Topic list
                        System.out.println("\nTopics:\n-------");

                        for (String name: forum.getTopics()) {
                            System.out.println(" - " + name);
                        }
                    } else if (op.equals("t")) { // Show messages
                        System.out.print("\nInsert the topic name: ");
                        topic = in.readLine();

                        ArrayList<String> messages = forum.getMessages(topic);
                        System.out.println("\nMessages:\n---------");

                        if (messages.size() == 0)
                            System.out.println("No message found about '" + topic + "'!");
                        else {
                            for (String message: messages) {
                                System.out.println(" - " + message);
                            }
                        }
                    } else if (op.equals("a")) { // Add topic
                        System.out.print("\nInsert the topic name: ");
                        topic = in.readLine();
                        forum.addTopic(topic);
                        System.out.println("Topic '" + topic + "' added!");
                    } else if (op.equals("m")) { // Send message
                        String message;
                        System.out.print("\nInsert the topic name: ");
                        topic = in.readLine();
                        System.out.print("Type a message: ");
                        message = in.readLine();
                        forum.sendMessage(topic, message);
                        System.out.println("Message posted under '" + topic + "'.");
                    } else if (op.equals("s")) { // Subscribe
                        System.out.print("\nInsert the topic name: ");
                        topic = in.readLine();
                        forum.subscribe(subscriber, topic);
                        System.out.println("User subscribed!");
                    } else if (!op.equals("e")) { // Error
                        System.err.println("\nError: Illegal operation!");
                    }
                } catch (IOException e) {
                    System.err.println("\nError: " + e.getMessage());
                }
            } while(!op.equals("e"));

            System.out.println("\nClient stopped.");
        } catch (RemoteException e) {
            System.err.println("Error: " + e.getMessage());
        } catch (NotBoundException e) {
            System.err.println("Class not found: " + e.getMessage());
        } finally {
            try {
                // Unexport the Subscriber object before termination
                UnicastRemoteObject.unexportObject(user, true);
            } catch (NoSuchObjectException e) {
                System.err.println("Could not unexport: " + e.getMessage());
            }
        }
    }
}
