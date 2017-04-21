// Francesco Landolfi
// Matr. 444151
// fran.landofli@gmail.com

import org.junit.Before;
import org.junit.Test;

import simplesocial.server.OnlineUsers;
import simplesocial.server.User;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.List;

import static org.junit.Assert.*;

public class OnlineUsersTest {
    private OnlineUsers onlineUsers;
    private SocketAddress addr;
    private User user;
    private int oAuth;

    @Before
    public void setUp() throws Exception {
        onlineUsers = new OnlineUsers();
        user = new User("JohnDoe", "123456");
        addr = new InetSocketAddress(0);
        oAuth = onlineUsers.login(user, addr);
    }

    @Test
    public void setLogDuration() throws Exception {
        long time = System.currentTimeMillis();

        OnlineUsers.setLogDuration(time);
        OnlineUsers.setLogDuration(OnlineUsers.getLogDurationString());
        assertEquals(time, OnlineUsers.getLogDurationMillis());

        OnlineUsers.setLogDuration("PT24M");
    }

    @Test
    public void isLogged() throws Exception {
        assertEquals(user, onlineUsers.isLogged(user.getUsername(), oAuth));
        assertEquals(null, onlineUsers.isLogged(user.getUsername(), -1));
        assertEquals(null, onlineUsers.isLogged("", oAuth));
    }

    @Test
    public void updateList() throws Exception {
        List<String> removedUsers = onlineUsers.updateList();
        User friend = new User("Bob", "qwerty");

        assertTrue(removedUsers.isEmpty());

        removedUsers = onlineUsers.updateList();

        assertFalse(removedUsers.isEmpty());
        assertEquals(1, removedUsers.size());
        assertEquals(user.getUsername(), removedUsers.get(0));
        assertTrue(onlineUsers.updateList().isEmpty());

        oAuth = onlineUsers.login(user, addr);
        onlineUsers.login(friend, addr);
        removedUsers = onlineUsers.updateList();

        assertTrue(removedUsers.isEmpty());

        onlineUsers.setAlive(user.getUsername());
        removedUsers = onlineUsers.updateList();
        onlineUsers.setAlive(user.getUsername());

        assertFalse(removedUsers.isEmpty());
        assertEquals(1, removedUsers.size());
        assertEquals(friend.getUsername(), removedUsers.get(0));
    }

    @Test
    public void getUserAddress() throws Exception {
        assertEquals(addr, onlineUsers.getUserAddress(user.getUsername()));
        assertEquals(addr, onlineUsers.getUserAddress(user));
        assertEquals(null, onlineUsers.getUserAddress("Alina"));
        assertEquals(null, onlineUsers.getUserAddress(new User("", "")));
    }

    @Test
    public void logout() throws Exception {
        assertEquals(user, onlineUsers.isLogged(user.getUsername(), oAuth));

        onlineUsers.logout(user);

        assertEquals(null, onlineUsers.isLogged(user.getUsername(), oAuth));

        oAuth = onlineUsers.login(user, addr);

        assertEquals(user, onlineUsers.isLogged(user.getUsername(), oAuth));
    }
}