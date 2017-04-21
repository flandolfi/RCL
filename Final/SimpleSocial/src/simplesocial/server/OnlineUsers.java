// Francesco Landolfi
// Matr. 444151
// fran.landofli@gmail.com

package simplesocial.server;

import java.net.SocketAddress;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implements a database of all <b>SimpleSocial</b> {@link User}s that are online.
 * For each {@link User}, stores a record containing the {@link User}, its authorization token,
 * the login time, and its {@link SocketAddress}.
 *
 * @author Francesco Landolfi
 */
public class OnlineUsers {
    private static final Random random = new Random(System.currentTimeMillis());
    private static long logDuration = 1440000L; // 24 minutes

    /**
     * Sets the duration of the log. After that duration, the {@link User} must login again on
     * the server.
     *
     * @param logDuration the duration of the login, in milliseconds
     */
    public static void setLogDuration(long logDuration) {
        OnlineUsers.logDuration = logDuration;
    }

    /**
     * Sets the duration of the log. After that duration, the {@link User} must login again on
     * the server.<p>
     * The format accepted by this method is the ISO-8601 standard (<code>PnDTnHnMn.nS</code>)
     *
     * @param text the duration of the login, in ISO-8601 format (<code>PnDTnHnMn.nS</code>)
     * @see Duration#parse(CharSequence)
     */
    public static void setLogDuration(String text) {
        Duration duration = Duration.parse(text); // Format: PnDTnHnMn.nS (ISO-8601)
        setLogDuration(duration.toMillis());
    }

    /**
     * Returns the login duration, in milliseconds.
     *
     * @return the login duration, in milliseconds
     */
    public static String getLogDurationString() {
        return Duration.ofMillis(logDuration).toString();
    }

    /**
     * Returns the login duration, in <code>PnDTnHnMn.nS</code> format
     * (ISO-8601 standard).
     *
     * @return the login duration, in <code>PnDTnHnMn.nS</code> format
     * @see Duration#toString()
     */
    public static long getLogDurationMillis() {
        return logDuration;
    }

    // This class represents a record of the database
    private class LogInRecord {
        private final User user;
        private final long loginTime;
        private final int oAuth;
        private boolean isAlive;
        private final SocketAddress address;

        // Constructor
        public LogInRecord(User user, SocketAddress address) {
            this.user = user;
            this.loginTime = System.currentTimeMillis();
            this.oAuth = random.nextInt(Integer.MAX_VALUE);
            this.isAlive = true;
            this.address = address;
        }

        // Getters
        public int getOAuth() {
            return oAuth;
        }

        public User getUser() {
            return user;
        }

        public SocketAddress getAddress() {
            return address;
        }

        // Checks if given credentials are valid
        public boolean match(String username, int oAuth) {
            return this.user.getUsername().equals(username) && this.oAuth == oAuth;
        }

        // Checks if the log is expired (the user logged more than logDuration milliseconds ago)
        public boolean isExpired() {
            return System.currentTimeMillis() > loginTime + logDuration;
        }

        // Checks if the user has responded to the last Keep-Alive signal
        public synchronized boolean isAlive() {
            return isAlive;
        }

        // Sets isAlive flag (true if the user has responded to the last Keep-Alive signal,
        // false otherwise)
        public synchronized void setAliveFlag(boolean isAlive) {
            this.isAlive = isAlive;
        }
    }

    // For each online user, stores its username and its LogInRecord
    private final Map<String, LogInRecord> records;

    /**
     * OnlineUsers constructor.
     */
    public OnlineUsers() {
        this.records = new ConcurrentHashMap<>();
    }

    /**
     * Logs <code>user</code> in with the given <code>address</code>.
     *
     * @param user the {@link User} to log in
     * @param address the {@link User}'s {@link SocketAddress}
     * @return {@link User}'s <code>oAuth</code>, or -1 if an error occurs
     */
    public int login(User user, SocketAddress address) {
        if (user == null || address == null)
            return -1;

        logout(user);
        LogInRecord record = new LogInRecord(user, address);
        this.records.put(user.getUsername(), record);

        return record.getOAuth();
    }

    /**
     * Logs <code>user</code> out.
     *
     * @param user the {@link User} to log out
     */
    public void logout(User user) {
        if (user != null)
            logout(user.getUsername());
    }

    /**
     * Logs the {@link User} with the given <code>username</code> out.
     *
     * @param username the name of the {@link User} to log out
     */
    public void logout(String username) {
        this.records.remove(username);
    }

    /**
     * Checks if a {@link User} is logged given its <code>username</code> and
     * <code>oAuth</code>. Returns the {@link User} if it's logged, <code>null</code>
     * otherwise.
     *
     * @param username the name of the {@link User}
     * @param oAuth the {@link User}'s <code>oAuth</code>
     * @return the {@link User} if it's logged, <code>null</code> otherwise
     */
    public User isLogged(String username, int oAuth) {
        LogInRecord record = records.get(username);

        if (record != null && record.match(username, oAuth) && !record.isExpired())
                return record.getUser();

        return null;
    }

    /**
     * Sets the {@link User} with the given <code>username</code> alive (has responded
     * to the last Keep-Alive signal).
     *
     * @param username the name of the {@link User} to set alive
     */
    public void setAlive(String username) {
        LogInRecord record = records.get(username);

        if (record != null)
            record.setAliveFlag(true);
    }

    /**
     * Checks if the {@link User} with the given <code>username</code> is alive (has
     * responded to the last Keep-Alive signal).
     *
     * @param username the name of the {@link User}
     * @return <code>true</code> if the {@link User} is alive, <code>false</code>
     * otherwise
     */
    public boolean isAlive(String username) {
        LogInRecord record = records.get(username);

        return record != null && record.isAlive();
    }

    /**
     * Removes all the records of the {@link User}s that are not alive (have not
     * responded to the last Keep-Alive signal). Sets all other {@link User}s not
     * alive. Returns the {@link List} of the removed {@link User}s.
     *
     * @return the {@link List} of the removed {@link User}s
     */
    public List<String> updateList() {
        ArrayList<String> removedUsers = new ArrayList<>();

        // Loops on a new HashMap to avoid concurrent modifications
        (new HashMap<>(records)).forEach((String username, LogInRecord record) -> {
            if (record.isAlive() && !record.isExpired())
                record.setAliveFlag(false);
            else {
                removedUsers.add(username);
                records.remove(username);
            }
        });

        return removedUsers;
    }

    /**
     * Returns the {@link SocketAddress} of the {@link User} with the given
     * <code>username</code>.
     *
     * @param username the name of the {@link User}
     * @return a {@link SocketAddress} if the {@link User} is online, <code>null</code>\
     * otherwise
     */
    public SocketAddress getUserAddress(String username) {
        LogInRecord record = records.get(username);

        if (record != null)
            return record.getAddress();

        return null;
    }

    /**
     * Returns the {@link SocketAddress} of the given {@link User} <code>user</code>.
     *
     * @param user the {@link User}
     * @return a {@link SocketAddress} if the {@link User} is online, <code>null</code>\
     * otherwise
     */
    public SocketAddress getUserAddress(User user) {
        return user == null? null : getUserAddress(user.getUsername());
    }
}