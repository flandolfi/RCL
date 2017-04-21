// Francesco Landolfi
// Matr. 444151
// fran.landolfi@gmail.com

import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// Student/Main class
public class Student implements Callable<Integer> {
    private int id;

    public Student(int id) {
        this.id = id;
    }

    @Override
    public Integer call() throws Exception {
        Random rand = new Random(System.currentTimeMillis());

        System.out.println("Student-" + id + " has entered the dining hall");
        Thread.sleep(rand.nextInt(4000) + 1000); // Wait 1-5 seconds
        System.out.println("Student-" + id + " has finished eating");

        return id;
    }

    public static void main(String[] args) {
        final int N = 10; // Number of students
        final int WASHING_THREADS = 3; // Number of washing threads in the pool
        final int RINSING_THREADS = 4; // Number of rinsing threads in the pool
        ExecutorService students = Executors.newCachedThreadPool();
        ExecutorService washers = Executors.newFixedThreadPool(WASHING_THREADS);
        ExecutorService rinsers = Executors.newFixedThreadPool(RINSING_THREADS);
        ExecutorCompletionService<Integer> studentsECS =
                new ExecutorCompletionService<>(students);
        ExecutorCompletionService<Integer> washersECS =
                new ExecutorCompletionService<>(washers);
        ExecutorCompletionService<Integer> rinsersECS =
                new ExecutorCompletionService<>(rinsers);
        
        try {
            Random rand = new Random(System.currentTimeMillis());

            // Start a new student task, then wait 0-5 seconds;
            // for each student, submit a new washer task and
            // a new rinsing task.
            for (int i = 0; i < N; i++) {
                rinsersECS.submit(new RinsingTask(washersECS));
                washersECS.submit(new WashingTask(studentsECS));
                studentsECS.submit(new Student(i));
                Thread.sleep(rand.nextInt(5000));
            }

            // Wait for all the rinsing tasks to complete
            for (int i = 0; i < N; i++)
                rinsersECS.take();

            System.out.println("\nAll dishes are clean!");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            students.shutdown();
            washers.shutdown();
            rinsers.shutdown();
        }
    }
}
