// Francesco Landolfi
// Matr. 444151
// fran.landofli@gmail.com

package simplesocial.client;

import simplesocial.Follower;
import simplesocial.Post;

import java.rmi.RemoteException;
import java.util.concurrent.BlockingQueue;

/**
 * An RMI callback. Implements {@link Follower} interface. Represents a
 * {@link simplesocial.server.User User}'s follower (i.e. another online
 * <b>SimpleSocial</b> client).
 *
 * @author Francesco Landolfi
 */
public class PostNotifier implements Follower {
    private final BlockingQueue<Post> posts;

    /**
     * {@link PostNotifier} constructor. The given <code>posts</code> queue
     * will be updated every time the callback will be called (i.e. a followed
     * {@link simplesocial.server.User User} posts something).
     *
     * @param posts a {@link BlockingQueue} of posts
     */
    public PostNotifier(BlockingQueue<Post> posts) {
        this.posts = posts;
    }

    /**
     * Sends a {@link Post} to the follower. The given {@link Post} will be
     * stored in the queue passed to the constructor.
     *
     * @param post the {@link Post} to be sent
     * @throws RemoteException communication-related exception
     */
    @Override
    public void sendUpdate(Post post) throws RemoteException {
        try {
            posts.put(post);
        } catch (InterruptedException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}
