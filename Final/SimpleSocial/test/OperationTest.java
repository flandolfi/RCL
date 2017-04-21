// Francesco Landolfi
// Matr. 444151
// fran.landofli@gmail.com

import static org.junit.Assert.*;

import org.junit.Test;

import simplesocial.Operation;

public class OperationTest {
    @Test
    public void conversionTest() {
        for (Operation op: Operation.values()) {
            assertEquals(op, Operation.getOperationFromInt(op.toInt()));
        }

        try {
            Operation.getOperationFromInt(-1);
            fail();
        } catch (IllegalArgumentException e) {
            // OK
        }
    }
}