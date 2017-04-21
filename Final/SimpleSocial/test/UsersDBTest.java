// Francesco Landolfi
// Matr. 444151
// fran.landofli@gmail.com

import org.junit.Before;
import org.junit.Test;

import simplesocial.server.User;
import simplesocial.server.UsersDB;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class UsersDBTest {
    private UsersDB usersDB;
    private User u1, u2, u3;

    @Before
    public void setUp() throws Exception {
        usersDB = new UsersDB();
        u1 = usersDB.register("user1", "123456");
        u2 = usersDB.register("user2", "123456");
        u3 = usersDB.register("user3", "123456");
    }

    @Test
    public void register() throws Exception {
        assertNotEquals(null, u1);
        assertNotEquals(null, u2);
        assertNotEquals(null, u3);
        assertNotEquals(null, usersDB.register("John_Lennon", "123456"));
        assertNotEquals(null, usersDB.register("Paul-MC", "123456"));
        assertNotEquals(null, usersDB.register("G.H.", "123456"));
        assertNotEquals(null, usersDB.register("RNGSTRR40", "123456"));
        assertEquals(null, usersDB.register("user1", "qwerty"));
        assertEquals(null, usersDB.register("user2", "qwerty"));
        assertEquals(null, usersDB.register("user3", "qwerty"));
        assertEquals(null, usersDB.register("user4", ""));
        assertEquals(null, usersDB.register("user5", "12345"));
        assertEquals(null, usersDB.register("user6", "qwe rty"));
        assertEquals(null, usersDB.register("Al", "qwerty"));
        assertEquals(null, usersDB.register("user@domain.com", "qwerty"));
        assertEquals(null, usersDB.register("", "qwerty"));
        assertEquals(null, usersDB.register("aaaaaaaaaa" +
                "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
                "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
                "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
                "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "qwerty"));
    }

    @Test
    public void getUserByName() throws Exception {
        assertEquals(null, usersDB.getUserByName("Alina"));
        assertEquals(null, usersDB.getUserByName("Bob"));
        assertEquals(null, usersDB.getUserByName("Catherine"));
        assertEquals(null, usersDB.getUserByName("user"));
        assertEquals(null, usersDB.getUserByName("User1"));
        assertEquals(u1, usersDB.getUserByName("user1"));
        assertEquals(u2, usersDB.getUserByName("user2"));
        assertEquals(u3, usersDB.getUserByName("user3"));
    }

    @Test
    public void isRegistered() throws Exception {
        assertTrue(usersDB.isRegistered("user1"));
        assertTrue(usersDB.isRegistered("user2"));
        assertTrue(usersDB.isRegistered("user3"));
        assertFalse(usersDB.isRegistered("User1"));
        assertFalse(usersDB.isRegistered("Alina"));
        assertFalse(usersDB.isRegistered("Bob"));
        assertFalse(usersDB.isRegistered("Catherine"));
    }

    @Test
    public void search() throws Exception {
        List<String> result = usersDB.search("user");

        assertEquals(3, result.size());
        assertTrue(result.contains("user1"));
        assertTrue(result.contains("user2"));
        assertTrue(result.contains("user3"));

        result = usersDB.search("SER");

        assertEquals(3, result.size());
        assertTrue(result.contains("user1"));
        assertTrue(result.contains("user2"));
        assertTrue(result.contains("user3"));

        result = usersDB.search("R2");

        assertEquals(1, result.size());
        assertTrue(result.contains("user2"));

        result = usersDB.search("Alina");

        assertTrue(result.isEmpty());
    }
}