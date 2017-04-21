// Francesco Landolfi
// Matr. 444151
// fran.landolfi@gmail.com

// Conference class
public class Conference {
    private String speaker; // Name of the student that is speaking
    private int listeners; // Number of students that are listening to the speaker
    private int count = 0; // Number of students that have already listened the speech

    // Constructor; must have the total number of students joining the conference
    public Conference(int listeners) {
        this.speaker = "";
        this.listeners = listeners;
    }

    // The student 'name' gives a new speech. This method decrements the number of
    // listeners (the speaker doesn't listen to himself!) and resets 'count'
    public synchronized void giveSpeech(String name) {
        this.speaker = name;
        this.listeners--;
        this.count = 0;
    }

    // Ends a speech (there is no student speaking)
    public synchronized void endSpeech() {
        this.speaker = "";
    }

    // Check if there is a student speaking
    public synchronized boolean isSpeaking() {
        return !speaker.equals("");
    }

    // Listen to a speech (increment 'count')
    public synchronized void listen() {
        this.count++;
    }

    // Name getter
    public synchronized String getSpeakerName() {
        return speaker;
    }

    // If 'count' is equal to the number of listeners at the conference, then
    // the speech has been listened
    public synchronized boolean speechHasBeenListened() {
        return listeners == count;
    }
}
