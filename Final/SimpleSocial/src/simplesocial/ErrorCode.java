// Francesco Landolfi
// Matr. 444151
// fran.landofli@gmail.com

package simplesocial;

/**
 * This enum helps identify an error. It is used mainly as a reply from a
 * <b>SimpleSocial</b> server to its clients.
 *
 * @author Francesco Landolfi
 */
public enum ErrorCode {
    SUCCESS,
    FAIL,
    CONNECTION_ERROR,
    USER_NOT_REGISTERED,
    USER_NOT_LOGGED,
    WRONG_PASSWORD,
    TARGET_USER_DOES_NOT_EXIST,
    USER_ALREADY_FRIEND,
    REQUEST_ALREADY_SENT,
    REQUEST_ALREADY_RECEIVED,
    TARGET_USER_IS_NOT_ONLINE,
    REQUEST_CANNOT_BE_SENT,
    REQUEST_CANNOT_BE_ACCEPTED;

    /**
     * Converts the {@link ErrorCode} to an {@link Integer}. The resulting
     * value will be negative, except for {@link ErrorCode#SUCCESS SUCCESS},
     * that will be 0.
     * <p>
     * The resulting value can be translated back to {@link ErrorCode} using
     * the method {@link ErrorCode#getCodeFromInt(int)}.
     *
     * @return the integer corresponding to the {@link ErrorCode}
     */
    public int toInt() {
        return -this.ordinal();
    }

    /**
     * Converts an {@link Integer} to its corresponding {@link ErrorCode}.
     * <p>
     * <b>Note</b>: this methods accepts values from -12 to 0. Other values
     * will throw an {@link IllegalArgumentException}.
     *
     * @param code the {@link Integer} to be converted
     * @return the resulting {@link ErrorCode}
     */
    public static ErrorCode getCodeFromInt(int code) {
        try {
            return ErrorCode.values()[-code];
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new IllegalArgumentException("Wrong error number.");
        }
    }
}
