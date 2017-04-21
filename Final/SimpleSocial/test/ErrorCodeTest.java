// Francesco Landolfi
// Matr. 444151
// fran.landofli@gmail.com

import static org.junit.Assert.*;

import org.junit.Test;

import simplesocial.ErrorCode;

public class ErrorCodeTest {
    @Test
    public void conversionTest() {
        for (ErrorCode code: ErrorCode.values()) {
            assertEquals(code, ErrorCode.getCodeFromInt(code.toInt()));
        }

        try {
            ErrorCode.getCodeFromInt(1);
            fail();
        } catch (IllegalArgumentException e) {
            // OK
        }
    }
}