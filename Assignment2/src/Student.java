// Francesco Landolfi
// Matr. 444151
// fran.landolfi@gmail.com

// Student/Main class
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
            synchronized (conference) {
                // If someone has made a speech, listen to it
                while (conference.isSpeaking()) {
                    System.out.println(name + " is listening to "
                            + conference.getSpeakerName() + "...");
                    conference.listen();

                    // If the speech has been listened, notify the speaker thread
                    if (conference.speechHasBeenListened())
                        conference.notify();

                    // Wait for a new speech
                    conference.wait();
                }

                // Nobody is talking; give a new speech
                conference.giveSpeech(name);
                System.out.println(name + " says: «Hi! My name is " + name + "».");

                // Notify all the consumers (listeners)
                conference.notifyAll();

                // Wait until the speech is heard by all listeners
                while (!conference.speechHasBeenListened())
                    conference.wait();

                // End the speech; notify the next speaker-to-be
                System.out.println(name + " says: «Thank you».");
                conference.endSpeech();
                conference.notify();
            }

            System.out.println(name + " has left the conference.");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // Main
    public static void main(String[] args) {
        final int N = 10;
        Conference course = new Conference(N);

        try {
            // Start N thread(s)
            for (int i = 0; i < N; i++)
                (new Thread(new Student(course))).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
