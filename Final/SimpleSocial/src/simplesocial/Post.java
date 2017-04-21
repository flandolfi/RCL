// Francesco Landolfi
// Matr. 444151
// fran.landofli@gmail.com

package simplesocial;

import java.io.Serializable;

/**
 * This class represents a post sent by a <b>SimpleSocial</b>
 * {@link simplesocial.server.User User}.
 *
 * @author Francesco Landolfi
 */
public class Post implements Serializable {
    private static final long serialVersionUID = 3L;
    private final String username;
    private final String post;

    /**
     * The {@link Post} constructor. <code>username</code> is the
     * sender's name, <code>post</code> is the content of the post.
     *
     * @param username name of the sender
     * @param post the content of the {@link Post}
     */
    public Post(String username, String post) {
        this.username = username;
        this.post = post;
    }

    /**
     * Returns the username.
     *
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Returns the content of the {@link Post}.
     *
     * @return the content of the {@link Post}
     */
    public String getPost() {
        return post;
    }

    /**
     * Returns a {@link String} version of the {@link Post}.
     *
     * @return a {@link String} version of the {@link Post}
     */
    public String toString() {
        return username + ": «" + post + "»";
    }
}
