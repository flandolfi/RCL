// Francesco Landolfi
// Matr. 444151
// fran.landolfi@gmail.com

// Student class
public class Student implements Runnable {
    private String name;
    private final Conference conference;

    public Student(Conference conference) {
        this.name = "";
        this.conference = conference;
    }

    @Override
    public void run() {
        try {
            this.name = Thread.currentThread().getName();
            System.out.println(name + " has entered the conference.");

            // Critical section (both producer and consumer)
            conference.getLock().lockInterruptibly();

            try {
                // If someone has made a speech, listen to it
                while (conference.isSpeaking()) {
                    System.out.println(name + " is listening to "
                            + conference.getSpeakerName() + "...");
                    conference.listen();

                    // If the speech has been listened, notify the speaker thread
                    if (conference.speechHasBeenListened())
                        conference.getSpeakCondition().signal();

                    // Wait for a new speech
                    conference.getListenCondition().await();
                }

                // Nobody is talking; give a new speech
                conference.giveSpeech(name);
                System.out.println(name + " says: «Hi! My name is " + name + "».");

                // Notify all the consumers (listeners)
                conference.getListenCondition().signalAll();

                // Wait until the speech is heard by all listeners
                while (!conference.speechHasBeenListened())
                    conference.getSpeakCondition().await();

                // End the speech; notify the next speaker-to-be
                System.out.println(name + " says: «Thank you».");
                conference.endSpeech();
                conference.getListenCondition().signal();
            } finally {
                conference.getLock().unlock();
            }

            System.out.println(name + " has left the conference.");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
