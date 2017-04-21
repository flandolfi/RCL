// Francesco Landolfi
// Matr. 444151
// fran.landolfi@gmail.com

import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorCompletionService;

// RinsingTask class
public class RinsingTask implements Callable<Integer> {
    ExecutorCompletionService<Integer> washers;

    public RinsingTask(ExecutorCompletionService<Integer> washers) {
        this.washers = washers;
    }

    @Override
    public Integer call() throws Exception {
        Random rand = new Random(System.currentTimeMillis());
        String name = "Rinser-" + Thread.currentThread().getId();
        int id = washers.take().get(); // Wait for a washed dish

        System.out.println(name + " is rinsing Student-" + id + "'s dish");
        Thread.sleep(rand.nextInt(4000) + 1000); // Wait 1-5 seconds
        System.out.println(name + " had rinsed Student-" + id + "'s dish");

        return id;
    }
}
