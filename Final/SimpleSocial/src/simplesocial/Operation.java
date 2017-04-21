// Francesco Landolfi
// Matr. 444151
// fran.landofli@gmail.com

package simplesocial;

/**
 * This enum helps identify an operation. It is used mainly as a message
 * from a <b>SimpleSocial</b> client to the server.
 *
 * @author Francesco Landolfi
 */
public enum Operation {
    REGISTER,
    LOGIN,
    ADD_FRIEND,
    CONFIRM_FRIENDSHIP,
    GET_FRIENDS_LIST,
    SEARCH,
    POST,
    LOGOUT;

    /**
     * Converts the {@link Operation} to an {@link Integer}.
     * <p>
     * The resulting value can be translated back to {@link Operation} using
     * the method {@link Operation#getOperationFromInt(int)}.
     *
     * @return the integer corresponding to the {@link Operation}
     */
    public int toInt() {
        return this.ordinal();
    }

    /**
     * Converts an {@link Integer} to its corresponding {@link Operation}.
     * <p>
     * <b>Note</b>: this methods accepts values from 0 to 7. Other values
     * will throw an {@link IllegalArgumentException}.
     *
     * @param op the {@link Integer} to be converted
     * @return the resulting {@link Operation}
     */
    public static Operation getOperationFromInt(int op) {
        try {
            return Operation.values()[op];
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new IllegalArgumentException("Wrong operation number.");
        }
    }
}
