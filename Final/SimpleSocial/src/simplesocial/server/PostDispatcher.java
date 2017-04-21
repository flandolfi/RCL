// Francesco Landolfi
// Matr. 444151
// fran.landofli@gmail.com

package simplesocial.server;

import simplesocial.*;

import java.rmi.RemoteException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class manages the subscriptions to {@link User}'s publications (followings).
 * Implements {@link Updater} interface.
 *
 * @author Francesco Landolfi
 */
public class PostDispatcher implements Updater {
    private final Map<String, Follower> registrations;
    private final OnlineUsers onlineUsers;
    private final UsersDB usersDB;

    /**
     * {@link PostDispatcher} constructor.
     *
     * @param onlineUsers the records of the {@link User}s that are currently online
     * @param usersDB the {@link User}s database
     */
    public PostDispatcher(OnlineUsers onlineUsers, UsersDB usersDB) {
        registrations = new ConcurrentHashMap<>();
        this.onlineUsers = onlineUsers;
        this.usersDB = usersDB;
    }

    /**
     * Registers <code>username</code> to the server (if <code>oAuth</code> is valid).
     * A registered {@link User} will be notified trough the <code>follower</code> callback.
     * If a {@link User} is successfully registered, it will receive all unread message it received
     * while it was offline.
     *
     * @param follower the {@link User}'s callback
     * @param username the {@link User}'s name
     * @param oAuth the {@link User}'s authorization token
     * @return <code>true</code> if registered, <code>false</code> otherwise
     * @throws RemoteException communication-related exception
     */
    @Override
    public synchronized boolean register(Follower follower, String username, int oAuth) throws RemoteException {
        if (follower == null)
            return false;

        User user = onlineUsers.isLogged(username, oAuth);

        if (user == null)
            return false;

        registrations.put(username, follower);

        for (Post post: user.getUnreadPosts()) {
            follower.sendUpdate(post);
        }

        return true;
    }

    /**
     * Unregisters <code>username</code> (remove its callback from the database).
     *
     * @param username the {@link User} to unregister
     * @return <code>true</code> if the {@link User} is unregistered, <code>false</code> if an
     * error occurs
     */
    public synchronized boolean unregister(String username) {
        return registrations.remove(username) != null;
    }

    /**
     * Makes <code>followerName</code> follow <code>username</code> (if <code>oAuth</code>
     * is valid). Returns an {@link ErrorCode}, which can have one of the following values:
     * <ul>
     *     <li>{@link ErrorCode#SUCCESS}, if <code>username</code> is now followed by
     *     <code>followerName</code>;</li>
     *     <li>{@link ErrorCode#TARGET_USER_DOES_NOT_EXIST}, if the {@link User} to follow is not found
     *     in the {@link UsersDB};</li>
     *     <li>{@link ErrorCode#USER_NOT_LOGGED}, if <code>followerName</code> and <code>oAuth</code>
     *     credentials are not valid or expired.</li>
     * </ul>
     *
     * @param username the {@link User} to be followed
     * @param followerName the follower's name
     * @param oAuth the follower's authorization token
     * @return an {@link ErrorCode}
     * @throws RemoteException communication-related exception
     */
    @Override
    public ErrorCode follow(String username, String followerName, int oAuth) throws RemoteException {
        User user = usersDB.getUserByName(username);
        User follower = onlineUsers.isLogged(followerName, oAuth);

        if (user == null)
            return ErrorCode.TARGET_USER_DOES_NOT_EXIST;

        if (follower == null)
            return ErrorCode.USER_NOT_LOGGED;

        if (user.addFollower(follower))
            return ErrorCode.SUCCESS;

        return ErrorCode.FAIL;
    }

    /**
     * Sends a {@link Post} to <code>followerName</code> through its callback.
     *
     * @param followerName the name of the {@link User}
     * @param post the {@link Post} to send
     * @return <code>true</code> if the {@link Post} has been sent, <code>false</code> otherwise
     * @throws RemoteException communication-related exception
     */
    public boolean sendUpdate(String followerName, Post post) throws RemoteException {
        Follower follower = registrations.get(followerName);

        if (follower != null) {
            follower.sendUpdate(post);

            return true;
        }

        return false;
    }

    /**
     * Sends a {@link Post} to all <code>poster</code>'s followers.
     *
     * @param poster the name of the sender {@link User}
     * @param content the content of the {@link Post} to be sent
     * @throws RemoteException communication-related exception
     */
    public void post(User poster, String content) throws RemoteException {
        if (poster != null) {
            Post post = new Post(poster.getUsername(), content);

            for (User user : poster.getFollowersList())
                if (!sendUpdate(user.getUsername(), post))
                    user.addPost(post);
        }
    }
}