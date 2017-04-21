// Francesco Landolfi
// Matr. 444151
// fran.landofli@gmail.com

package simplesocial.server;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Implements a <b>SimpleSocial</b> {@link User}s database.
 *
 * @author Francesco Landolfi
 */
public class UsersDB implements Serializable {
    private static final long serialVersionUID = 2L;
    private final ArrayList<User> users;
    private final ReentrantReadWriteLock lock;

    /**
     * {@link UsersDB} constructor. Creates a new {@link User} database.
     */
    public UsersDB() {
        this.users = new ArrayList<>();
        this.lock = new ReentrantReadWriteLock();
    }

    /**
     * Registers a new {@link User} to the database with the given credentials. If the
     * registration was successful, returns the registered {@link User}, otherwise
     * returns <code>null</code>. Note that:
     * <ul>
     *     <li><code>username</code> must not be already registered;</li>
     *     <li><code>username</code> and <code>password</code> must contains only
     *     alphanumerics, '-', '_', and '.';</li>
     *     <li><code>username</code> must be 3 to 50 characters long;</li>
     *     <li><code>password</code> must be 3 to 50 characters long.</li>
     * </ul>
     *
     * @param username the name of the {@link User} to be registered
     * @param password the password used by the {@link User}
     * @return an {@link User} if the registration was successful, <code>null</code>
     * otherwise
     */
    public User register(String username, String password) {
        if (username == null || password == null || !username.matches("^[a-z0-9A-Z._-]{3,50}$") ||
                !password.matches("^[a-z0-9A-Z._-]{6,50}$") || isRegistered(username))
            return null;

        this.lock.writeLock().lock();
        User user = new User(username, password);
        this.users.add(user);
        this.lock.writeLock().unlock();

        return user;
    }

    /**
     * Searches the database for the {@link User} named <code>username</code>. If exists,
     * returns the {@link User}, otherwise returns <code>null</code>.
     *
     * @param username the name of the {@link User}
     * @return the {@link User} whose name is <code>username</code> if exists,
     * <code>null</code> otherwise.
     */
    public User getUserByName(String username) {
        this.lock.readLock().lock();

        for (User user: users) {
            if (user.getUsername().equals(username)) {
                this.lock.readLock().unlock();

                return user;
            }
        }

        this.lock.readLock().unlock();

        return null;
    }

    /**
     * Checks if a {@link User} is registered.
     *
     * @param username the name of the {@link User}
     * @return <code>true</code> if registered, <code>false</code> otherwise
     */
    public boolean isRegistered(String username) {
        return getUserByName(username) != null;
    }

    /**
     * Searches for {@link User} whose usernames contain the string <code>str</code>.
     *
     * @param str the string to be searched
     * @return a {@link List} of {@link User}s
     */
    public List<String> search(String str) {
        ArrayList<String> usernames = new ArrayList<>();

        this.lock.readLock().lock();
        users.forEach((User user) -> {
            if (user.getUsername().toLowerCase().contains(str.toLowerCase()))
                usernames.add(user.getUsername());
        });
        this.lock.readLock().unlock();

        return usernames;
    }
}
