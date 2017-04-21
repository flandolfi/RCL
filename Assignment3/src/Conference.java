// Francesco Landolfi
// Matr. 444151
// fran.landolfi@gmail.com

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

// Conference/Main class
public class Conference {
    private String speaker; // Name of the student that is speaking
    private int listeners; // Number of students that are listening to the speaker
    private int count = 0; // Number of students that have already listened the speech
    private ReentrantLock lock; // Lock
    private Condition speakCondition, listenCondition; // Conditions

    // Constructor; must have the total number of students joining the conference
    public Conference(int listeners) {
        this.speaker = "";
        this.listeners = listeners;
        this.lock = new ReentrantLock(true);
        this.speakCondition = this.lock.newCondition();
        this.listenCondition = this.lock.newCondition();
    }

    // Getters
    public String getSpeakerName() {
        return speaker;
    }

    public ReentrantLock getLock() {
        return lock;
    }

    public Condition getSpeakCondition() {
        return speakCondition;
    }

    public Condition getListenCondition() {
        return listenCondition;
    }

    // The student 'name' gives a new speech. This method decrements the number of
    // listeners (the speaker doesn't listen to himself!) and resets 'count'
    public void giveSpeech(String name) throws InterruptedException {
        this.lock.lockInterruptibly();
        this.speaker = name;
        this.listeners--;
        this.count = 0;
        this.lock.unlock();
    }

    // Ends a speech (there is no student speaking)
    public void endSpeech() throws InterruptedException {
        this.lock.lockInterruptibly();
        this.speaker = "";
        this.lock.unlock();
    }

    // Check if there is a student speaking
    public boolean isSpeaking() throws InterruptedException {
        this.lock.lockInterruptibly();
        boolean result = !speaker.equals("");
        this.lock.unlock();

        return result;
    }

    // Listen to a speech (increment 'count')
    public void listen() throws InterruptedException {
        this.lock.lockInterruptibly();
        this.count++;
        this.lock.unlock();
    }

    // If 'count' is equal to the number of listeners at the conference, then
    // the speech has been listened
    public boolean speechHasBeenListened() throws InterruptedException {
        this.lock.lockInterruptibly();
        boolean result = (listeners == count);
        this.lock.unlock();

        return result;
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
