// Francesco Landolfi
// Matr. 444151
// fran.landofli@gmail.com

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

// Client class
public class Client {

    // Main
    public static void main(String[] args) {
        try {
            Conference conference = (Conference) LocateRegistry.getRegistry(Server.PORT)
                    .lookup(Conference.OBJECT_NAME);
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            String op = "";

            // Ask for an operation:
            //  - P: Print the full conference programme;
            //  - R: Register a speaker, after its name, the conference day and
            //       the session number have been inserted (if a wrong day/session
            //       number is selected, an exception will be thrown and handled);
            //  - E: Exit and terminate this program.
            do {
                try {
                    System.out.println("Select an operation [P|R|E]\n" +
                            "(P = Print Programme/R = Register a speaker/E = Exit): ");
                    op = in.readLine().trim().toLowerCase();

                    if (op.equals("p")) {
                        System.out.print(conference.getProgramme());
                    } else if (op.equals("r")) {
                        String speaker;
                        int day, session;

                        System.out.print("Insert the name of the speaker: ");
                        speaker = in.readLine();
                        System.out.print("Insert the day: ");
                        day = new Integer(in.readLine());
                        System.out.print("Insert the session number: ");
                        session = new Integer(in.readLine());
                        conference.register(day, session, speaker);
                        System.out.println("Speaker registered!");
                    } else if (!op.equals("e")) {
                        System.err.println("Error: Illegal operation!");
                    }
                } catch (RemoteException e) {
                    System.err.println("Error: " + e.getMessage());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } while(!op.equals("e"));

            System.out.println("Client stopped.");
        } catch (RemoteException e) {
            System.err.println("Client error: " + e.getMessage());
        } catch (NotBoundException e) {
            System.err.println("Class not available: " + e.getMessage());
        }
    }
}
