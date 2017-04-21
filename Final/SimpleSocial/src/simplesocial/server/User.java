// Francesco Landolfi
// Matr. 444151
// fran.landofli@gmail.com

package simplesocial.server;

import simplesocial.Post;

import java.io.Serializable;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * A <b>SimpleSocial</b> user.
 *
 * @author Francesco Landolfi
 */
public class User implements Serializable {
    private static final long serialVersionUID = 1L;
    private static long requestDuration = 1440000L;
    private final String username;
    private final String password;
    private final ArrayList<User> friends;
    private final ArrayList<User> followers;
    private final HashMap<User, Long> requests;
    private final ArrayList<Post> missedPosts;

    /**
     * Sets the duration of the friends requests. If a friend request is not
     * within that duration, the request is expired and cannot be accepted anymore.
     *
     * @param duration the duration of a friend request, in milliseconds
     */
    public static void setFriendRequestDuration(long duration) {
        User.requestDuration = duration;
    }

    /**
     * Sets the duration of the friends requests. If a friend request is not
     * within that duration, the request is expired and cannot be accepted anymore.
     * <p>
     * The format accepted by this method is the ISO-8601 standard (<code>PnDTnHnMn.nS</code>)
     *
     * @param text the duration of a friend request, in ISO-8601 format (<code>PnDTnHnMn.nS</code>)
     * @see Duration#parse(CharSequence)
     */
    public static void setFriendRequestDuration(String text) {
        Duration duration = Duration.parse(text); // Format: PnDTnHnMn.nS (ISO-8601)
        setFriendRequestDuration(duration.toMillis());
    }

    /**
     * Returns the friend request duration, in milliseconds.
     *
     * @return the friend request duration, in milliseconds
     */
    public static long getFriendRequestDurationMillis() {
        return requestDuration;
    }

    /**
     * Returns the friend request duration, in <code>PnDTnHnMn.nS</code> format
     * (ISO-8601 standard).
     *
     * @return the friend request duration, in <code>PnDTnHnMn.nS</code> format
     * @see Duration#toString()
     */
    public static String getFriendRequestDurationString() {
        return Duration.ofMillis(requestDuration).toString();
    }

    /**
     * {@link User} constructor. <code>username</code> represents the name of the user,
     * <code>password</code> its password.
     *
     * @param username the name of the user
     * @param password the password used by the user
     */
    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.friends = new ArrayList<>();
        this.followers = new ArrayList<>();
        this.missedPosts = new ArrayList<>();
        this.requests = new HashMap<>();
    }

    /**
     * Returns the name of the user.
     *
     * @return the name of the user
     */
    public String getUsername() {
        return username;
    }

    /**
     * Checks if the given <code>password</code> is the same of {@link User}'s.
     *
     * @param password a password
     * @return <code>true</code> if <code>password</code> is the same of the {@link User},
     * <code>false</code> otherwise.
     */
    public boolean checkPassword(String password) {
        return this.password.compareTo(password) == 0;
    }

    /**
     * Checks if <code>user</code> is a friend of this {@link User}.
     *
     * @param user the {@link User} to check
     * @return <code>true</code> if is a friend, <code>false</code> otherwise
     */
    public boolean isFriend(User user) {
        synchronized (friends) {
            return friends.contains(user);
        }
    }

    /**
     * Saves a friend request from the given <code>user</code>. Note that a request sent
     * from a {@link User} already friend or that has already sent a friend request will
     * not be saved.
     *
     * @param user the {@link User} who sent a request
     * @return <code>true</code> if the request is saved, <code>false</code> otherwise
     */
    public boolean saveRequest(User user) {
        synchronized (requests) {
            synchronized (friends) {
                if (isFriend(user) || hasPendingRequestFrom(user))
                    return false;

                // Saves the user and the time on which the request has been sent
                requests.put(user, System.currentTimeMillis());

                return true;
            }
        }
    }

    /**
     * Checks if this {@link User} has received a request from <code>user</code>.
     *
     * @param user the {@link User} who might have sent a request
     * @return <code>true</code> if <code>user</code> has sent a request, <code>false</code>
     * otherwise
     */
    public boolean hasPendingRequestFrom(User user) {
        synchronized (requests) {
            Long time = requests.get(user);

            // user is not present in requests Map
            if (time == null)
                return false;

            // Checks if request is valid
            if (System.currentTimeMillis() - time < requestDuration)
                return true;

            // If expired, remove the request
            requests.remove(user);

            return false;
        }
    }

    /**
     * Accepts a request from <code>user</code>. Note that a request can be accepted
     * only if it was already sent and is not expired.
     *
     * @param user the {@link User} who made a request
     * @return <code>true</code> if the request is accepted, <code>false</code> otherwise
     */
    public boolean acceptRequest(User user) {
        if (user == null)
            return false;

        synchronized (requests) {
            if (!hasPendingRequestFrom(user))
                return false;

            Object o1, o2;

            // Sets synchronized priority (avoids deadlocks)
            if (username.compareTo(user.getUsername()) > 0) {
                o1 = friends;
                o2 = user.friends;
            } else {
                o1 = user.friends;
                o2 = friends;
            }

            // Removes pending request and add user to this.friends and this to user.friends
            synchronized (o1) {
                synchronized (o2) {
                    requests.remove(user);
                    friends.add(user);
                    user.friends.add(this);

                    return true;
                }
            }
        }
    }

    /**
     * Saves <code>user</code> as a follower. Note that only friends can become followers.
     *
     * @param user the new follower
     * @return <code>true</code> if <code>user</code> is now a follower, <code>false</code>
     * otherwise
     */
    public boolean addFollower(User user) {
        synchronized (friends) {
            synchronized (followers) {
                if (!friends.contains(user) || followers.contains(user))
                    return false;

                followers.add(user);

                return true;
            }
        }
    }

    /**
     * Adds <code>post</code> to the received posts.
     *
     * @param post the {@link Post} to add
     */
    public void addPost(Post post) {
        synchronized (missedPosts) {
            missedPosts.add(post);
        }
    }

    /**
     * Returns a {@link List} of received and unread posts. Once this
     * method is called, the returned {@link Post}s are considered read.
     *
     * @return a {@link List} of received posts.
     */
    public List<Post> getUnreadPosts() {
        synchronized (missedPosts) {
            ArrayList<Post> posts = new ArrayList<>(missedPosts);
            missedPosts.clear();

            return posts;
        }
    }

    /**
     * Returns the {@link List} of the names of {@link User}'s friends.
     *
     * @return a {@link List} of usernames ({@link String}s)
     */
    public List<String> getFriendsList() {
        synchronized (friends) {
            ArrayList<String> result = new ArrayList<>();

            friends.forEach((User u) -> result.add(u.getUsername()));

            return result;
        }
    }

    /**
     * Returns the {@link List} of {@link User}'s followers.
     *
     * @return {@link User}'s followers
     */
    public List<User> getFollowersList() {
        synchronized (followers) {
            return new ArrayList<>(followers);
        }
    }
}
