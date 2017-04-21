// Francesco Landolfi
// Matr. 444151
// fran.landofli@gmail.com

import org.junit.Before;
import org.junit.Test;

import simplesocial.ErrorCode;
import simplesocial.Follower;
import simplesocial.Post;
import simplesocial.server.OnlineUsers;
import simplesocial.server.PostDispatcher;
import simplesocial.server.User;
import simplesocial.server.UsersDB;

import java.net.InetSocketAddress;
import java.rmi.RemoteException;
import java.util.ArrayList;

import static org.junit.Assert.*;

public class PostDispatcherTest {
    private ArrayList<Post> sentPosts;
    private UsersDB usersDB;
    private OnlineUsers onlineUsers;
    private PostDispatcher postDispatcher;
    private User u1, u2;
    private Post p1, p2, p3, p4;
    private int o1, o2;

    private class PseudoFollower implements Follower {
        @Override
        public void sendUpdate(Post post) throws RemoteException {
            sentPosts.add(post);
        }
    }

    @Before
    public void setUp() throws Exception {
        sentPosts  = new ArrayList<>();
        usersDB = new UsersDB();
        onlineUsers = new OnlineUsers();
        postDispatcher = new PostDispatcher(onlineUsers, usersDB);
        u1 = usersDB.register("Alina", "123456");
        u2 = usersDB.register("Bob", "123456");
        u1.saveRequest(u2);
        u1.acceptRequest(u2);
        u1.addFollower(u2);
        o1 = onlineUsers.login(u1, new InetSocketAddress(0));
        p1 = new Post("Alina", "Toodle-loo!");
        p2 = new Post("Alina", "Au revoir!");
        p3 = new Post("Alina", "Auf wiedersehen!");
        p4 = new Post("Alina", "Ciao!");
    }

    @Test
    public void register() throws Exception {
        assertTrue(postDispatcher.register(new PseudoFollower(), "Alina", o1));
        assertFalse(postDispatcher.register(new PseudoFollower(), "Bob", 0));
    }

    @Test
    public void unregister() throws Exception {
        assertTrue(postDispatcher.register(new PseudoFollower(), "Alina", o1));
        assertTrue(postDispatcher.unregister("Alina"));
        assertFalse(postDispatcher.unregister("Alina"));
        assertFalse(postDispatcher.unregister("Bob"));
        assertTrue(postDispatcher.register(new PseudoFollower(), "Alina", o1));
    }

    @Test
    public void follow() throws Exception {
        assertEquals(ErrorCode.USER_NOT_LOGGED,
                postDispatcher.follow(u1.getUsername(), u2.getUsername(), 0));

        o2 = onlineUsers.login(u2, new InetSocketAddress(0));

        assertEquals(ErrorCode.FAIL,
                postDispatcher.follow(u1.getUsername(), u2.getUsername(), o2));

        onlineUsers.logout(u2);

        assertEquals(ErrorCode.SUCCESS,
                postDispatcher.follow(u2.getUsername(), u1.getUsername(), o1));
        assertEquals(ErrorCode.TARGET_USER_DOES_NOT_EXIST,
                postDispatcher.follow("Catherine", u1.getUsername(), o1));
    }

    @Test
    public void sendUpdate() throws Exception {
        assertTrue(sentPosts.isEmpty());
        assertFalse(postDispatcher.sendUpdate("Catherine", p1));
        assertTrue(sentPosts.isEmpty());
        assertTrue(postDispatcher.register(new PseudoFollower(), "Alina", o1));
        assertTrue(postDispatcher.sendUpdate("Alina", p1));
        assertTrue(sentPosts.contains(p1));

        sentPosts.clear();
    }

    @Test
    public void post() throws Exception {
        postDispatcher.post(u1, p1.getPost());
        postDispatcher.post(u1, p2.getPost());
        postDispatcher.post(u1, p3.getPost());
        postDispatcher.post(u1, p4.getPost());

        assertTrue(sentPosts.isEmpty());

        o2 = onlineUsers.login(u2, new InetSocketAddress(0));

        assertTrue(postDispatcher.register(new PseudoFollower(), "Bob", o2));
        assertEquals(4, sentPosts.size());

        sentPosts.clear();
        postDispatcher.post(u1, p1.getPost());

        assertEquals(1, sentPosts.size());
    }

}
