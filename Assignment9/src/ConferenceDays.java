// Francesco Landolfi
// Matr. 444151
// fran.landofli@gmail.com

import java.rmi.RemoteException;
import java.rmi.server.RemoteObject;
import java.util.ArrayList;

// ConferenceDays class (implements the Conference stub)
public class ConferenceDays extends RemoteObject implements Conference {

    // Programme auxiliary class - A data structure that stores the programme
    // of a conference day (and all of its sessions and speakers)
    private class Programme {
        private static final int SESSIONS = 12; // Number of sessions per day
        private static final int SPEAKERS = 5; // Number of speakers per session
        private ArrayList<ArrayList<String>> sessions;

        // Constructor
        public Programme() {
            this.sessions = new ArrayList<>();

            for (int i = 0; i < SESSIONS; i++) {
                this.sessions.add(new ArrayList<>());
            }
        }

        // Register a speaker on a given session
        public void register(int session, String speakerName) throws RemoteException {
            if (session < 1 || session > SESSIONS)
                throw new RemoteException("Wrong session number.");
            else if (sessions.get(session - 1).size() >= SPEAKERS)
                throw new RemoteException("Selected session is full.");
            else
                sessions.get(session - 1).add(speakerName);
        }

        // Return a string containing the programme
        public String toString() {
            String result = "";
            int i = 1;

            for (ArrayList<String> session: sessions) {
                int j = 1;
                result += "Session " + i + ":\n";

                for (String speaker: session) {
                    result += "  " + j + ". " + speaker + "\n";
                    j++;
                }

                i++;
            }

            return result;
        }
    }

    private static final long serialVersionUID = 2L;
    private static final int DAYS = 3; // Duration of the conference
    private ArrayList<Programme> programmes;

    // Constructor
    public ConferenceDays() {
        this.programmes = new ArrayList<>();

        for (int i = 0; i < DAYS; i++) {
            programmes.add(new Programme());
        }
    }

    // Register a speaker on a given day and a given session
    @Override
    public void register(int day, int session, String speakerName) throws RemoteException {
        if (day < 1 || day > DAYS)
            throw new RemoteException("Wrong day number.");
        else {
            programmes.get(day - 1).register(session, speakerName);
            System.out.println("New speaker added @ Day " + day + ", Session " +
                    session + ": \"" + speakerName + "\"");
        }
    }

    // Return a string containing the full programme
    @Override
    public String getProgramme() {
        int i = 1;
        String result = "\n";

        System.out.println("Conference programme requested.");

        for (Programme day: programmes) {
            result += "Day " + i + "\n=====\n\n" + day + "\n";
            i++;
        }

        return result;
    }
}
