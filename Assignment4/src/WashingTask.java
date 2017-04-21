// Francesco Landolfi
// Matr. 444151
// fran.landolfi@gmail.com

import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorCompletionService;

// WashingTask class
public class WashingTask implements Callable<Integer> {
    ExecutorCompletionService<Integer> students;

    public WashingTask(ExecutorCompletionService<Integer> students) {
        this.students = students;
    }

    @Override
    public Integer call() throws Exception {
        Random rand = new Random(System.currentTimeMillis());
        String name = "Washer-" + Thread.currentThread().getId();
        int id = students.take().get(); // Wait for a dirty dish

        System.out.println(name + " is washing Student-" + id + "'s dish");
        Thread.sleep(rand.nextInt(4000) + 1000); // Wait 1-5 seconds
        System.out.println(name + " had washed Student-" + id + "'s dish");

        return id;
    }
}
