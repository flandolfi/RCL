// Francesco Landolfi
// Matr. 444151
// fran.landofli@gmail.com

import org.junit.Before;
import org.junit.Test;

import simplesocial.Post;
import simplesocial.server.User;

import java.util.List;

import static org.junit.Assert.*;

public class UserTest {
    private User user, f1, f2, f3, f4;

    @Before
    public void setUp() throws Exception {
        user = new User("JohnDoe", "123456");

        f1 = new User("Alina", "");
        f2 = new User("Bob", "");
        f3 = new User("Catherine", "");
        f4 = new User("Dylan", "");

        assertTrue(user.saveRequest(f1));
        assertTrue(user.saveRequest(f2));
        assertTrue(user.saveRequest(f3));
    }

    @Test
    public void friendRequestDuration() throws Exception {
        long time = System.currentTimeMillis();

        User.setFriendRequestDuration(time);
        User.setFriendRequestDuration(User.getFriendRequestDurationString());
        assertEquals(time, User.getFriendRequestDurationMillis());

        User.setFriendRequestDuration("PT24M");
    }

    @Test
    public void checkPassword() throws Exception {
        assertFalse(user.checkPassword(""));
        assertFalse(user.checkPassword("aaaaaaa"));
        assertTrue(user.checkPassword("123456"));
    }

    @Test
    public void friendships() throws Exception {
        User.setFriendRequestDuration("PT24M");

        assertTrue(user.hasPendingRequestFrom(f1));
        assertTrue(user.hasPendingRequestFrom(f2));
        assertTrue(user.hasPendingRequestFrom(f3));
        assertFalse(user.hasPendingRequestFrom(f4));

        assertFalse(user.isFriend(f1));
        assertFalse(user.isFriend(f2));
        assertFalse(user.isFriend(f3));

        assertTrue(user.acceptRequest(f1));
        assertTrue(user.acceptRequest(f2));
        assertTrue(user.acceptRequest(f3));
        assertFalse(user.acceptRequest(f4));

        assertTrue(user.isFriend(f1));
        assertTrue(user.isFriend(f2));
        assertTrue(user.isFriend(f3));
        assertFalse(user.isFriend(f4));

        User.setFriendRequestDuration(0);

        assertFalse(user.saveRequest(f1));
        assertFalse(user.saveRequest(f2));
        assertFalse(user.saveRequest(f3));
        assertTrue(user.saveRequest(f4));

        assertFalse(user.acceptRequest(f4));

        assertTrue(user.addFollower(f1));
        assertTrue(user.addFollower(f2));
        assertTrue(user.addFollower(f3));
        assertFalse(user.addFollower(f4));

        assertTrue(user.getFollowersList().contains(f1));
        assertTrue(user.getFollowersList().contains(f2));
        assertTrue(user.getFollowersList().contains(f3));
        assertFalse(user.getFollowersList().contains(f4));

        assertTrue(user.getFriendsList().contains(f1.getUsername()));
        assertTrue(user.getFriendsList().contains(f2.getUsername()));
        assertTrue(user.getFriendsList().contains(f3.getUsername()));
        assertFalse(user.getFriendsList().contains(f4.getUsername()));
    }

    @Test
    public void posts() throws Exception {
        Post p1, p2, p3, p4;
        List<Post> missedPosts;

        p1 = new Post(f1.getUsername(), "Toodle-loo!");
        p2 = new Post(f2.getUsername(), "Au revoir!");
        p3 = new Post(f3.getUsername(), "Auf wiedersehen");
        p4 = new Post(f4.getUsername(), "Ciao!");

        user.addPost(p1);
        user.addPost(p2);
        user.addPost(p3);
        user.addPost(p4);

        missedPosts = user.getUnreadPosts();

        assertTrue(missedPosts.contains(p1));
        assertTrue(missedPosts.contains(p2));
        assertTrue(missedPosts.contains(p3));
        assertTrue(missedPosts.contains(p4));

        missedPosts = user.getUnreadPosts();

        assertFalse(missedPosts.contains(p1));
        assertFalse(missedPosts.contains(p2));
        assertFalse(missedPosts.contains(p3));
        assertFalse(missedPosts.contains(p4));
    }
}