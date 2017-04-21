// Francesco Landolfi
// Matr. 444151
// fran.landofli@gmail.com

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.RunWith;
import org.junit.runner.notification.Failure;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    ClientTest.class,
    ErrorCodeTest.class,
    OnlineUsersTest.class,
    OperationTest.class,
    PostDispatcherTest.class,
    ServerTest.class,
    UserTest.class,
    UsersDBTest.class
})
public class TestSuite {
    public static void main(String[] args) {
        System.out.println("Starting test suite...");

        Result result = JUnitCore.runClasses(TestSuite.class);

        for (Failure failure: result.getFailures()) {
            System.err.println("FAIL: " + failure.toString());
        }

        if (result.wasSuccessful())
            System.out.println("\nAll tests were successful!");

        System.exit(0);
    }
}
