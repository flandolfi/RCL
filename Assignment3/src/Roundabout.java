// Francesco Landolfi
// Matr. 444151
// fran.landolfi@gmail.com

import java.util.ArrayList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

// Roundabout/Main class
public class Roundabout {
    private ArrayList<ReentrantLock> locks;
    private ArrayList<Condition> conditions;
    private int[] carsAt;

    public Roundabout() {
        this.locks = new ArrayList<>();
        this.conditions = new ArrayList<>();
        this.carsAt = new int[]{ 0, 0, 0, 0 };

        for (int i = 0; i < 4; i++) {
            ReentrantLock lock = new ReentrantLock(true);
            this.locks.add(lock);
            this.conditions.add(lock.newCondition());
        }
    }

    // Getters
    public ReentrantLock getLock(int i) {
        return locks.get(i);
    }

    public Condition getCondition(int i) {
        return conditions.get(i);
    }

    // Logger; just for debugging purposes (thread unsafe!)
    public void log(String message) {
        System.out.printf("[%02d:%02d:%02d:%02d] %s\n",
                carsAt[0], carsAt[1], carsAt[2], carsAt[3], message);
    }

    // Check if a section is free
    public boolean isSectionFree(int i) throws InterruptedException {
        this.locks.get(i).lockInterruptibly();
        boolean result = (carsAt[i] == 0);
        this.locks.get(i).unlock();

        return result;
    }

    // Enter a section
    public void enterSection(int i) throws InterruptedException {
        this.locks.get(i).lockInterruptibly();
        carsAt[i]++;
        this.locks.get(i).unlock();
    }

    // Leave a section
    public void leaveSection(int i) throws InterruptedException {
        this.locks.get(i).lockInterruptibly();
        carsAt[i]--;
        this.locks.get(i).unlock();
    }

    // Main
    public static void main(String[] args) {
        Roundabout roundabout = new Roundabout();
        final int N = 100;

        try {
            for (int i = 0; i < N; i++)
                (new Thread(new Car(roundabout))).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
