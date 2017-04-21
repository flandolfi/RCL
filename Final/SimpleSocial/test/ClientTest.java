// Francesco Landolfi
// Matr. 444151
// fran.landofli@gmail.com

import org.junit.*;

import java.net.InetAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

import org.junit.rules.TemporaryFolder;
import simplesocial.*;
import simplesocial.client.*;
import simplesocial.server.*;

import static org.junit.Assert.*;

public class ClientTest {
    private static ExecutorService serverEx;
    private static Client client1, client2;
    private static User u1, u2, u3, u4, u5, u6, u7, u8;

    @ClassRule
    public static TemporaryFolder serverDir = new TemporaryFolder();

    @ClassRule
    public static TemporaryFolder clientDir = new TemporaryFolder();

    @BeforeClass
    public static void setUp() throws Exception {
        Server.DATA_DIR_PATH = serverDir.getRoot().getPath();
        Client.DATA_DIR_PATH = clientDir.getRoot().getPath();
        client1 = new Client();
        client2 = new Client();

        UsersDB usersDB = new UsersDB();
        User l1, l2;
        u1 = usersDB.register("user1", "123456");
        u2 = usersDB.register("user2", "123456");
        u3 = usersDB.register("user3", "123456");
        u4 = usersDB.register("user4", "123456");
        u5 = usersDB.register("user5", "123456");
        u6 = usersDB.register("user6", "123456");
        u7 = usersDB.register("user7", "123456");
        u8 = usersDB.register("user8", "123456");

        for (int i = 0; i < 100; i++) {
            usersDB.register("dummy-user-n-" + i, "000000");
        }

        u1.saveRequest(u2);
        u1.acceptRequest(u2);
        u1.saveRequest(u3);
        u1.acceptRequest(u3);
        u1.addFollower(u2);
        u2.saveRequest(u3);
        u3.saveRequest(u4);
        u3.saveRequest(u8);
        u3.acceptRequest(u8);
        l1 = new User("VeryLongName1                               " +
                "                                                  " +
                "                                                  " +
                "                                                  " +
                "                                                  " +
                "                                                  " +
                "                                                  ", "");
        l2 = new User("VeryLongName2                               " +
                "                                                  " +
                "                                                  " +
                "                                                  " +
                "                                                  " +
                "                                                  " +
                "                                                  ", "");
        u6.saveRequest(l1);
        u6.acceptRequest(l1);
        u6.saveRequest(l2);
        u6.acceptRequest(l2);

        Server.storeUsersDB(usersDB);

        serverEx = Executors.newSingleThreadExecutor();
        serverEx.execute(() -> Server.main(new String[] {}));

        Thread.sleep(1000);
    }

    @AfterClass
    public static void tearDown() throws Exception {
        serverEx.shutdown();
    }

    @Test
    public void configuration() throws Exception {
        client1.storeConfiguration();

        client1.TCP_PORT = 0;
        client1.UDP_PORT = 0;
        client1.MC_PORT = 0;
        client1.RMI_PORT = 0;
        client1.SERVER_ADDR = "";
        client1.MC_GROUP = "";

        client1.loadConfiguration();

        assertEquals(2000, client1.TCP_PORT);
        assertEquals(2500, client1.UDP_PORT);
        assertEquals(3000, client1.MC_PORT);
        assertEquals(3500, client1.RMI_PORT);
        assertEquals(InetAddress.getLocalHost().getHostAddress(), client1.SERVER_ADDR);
        assertEquals("239.255.1.1", client1.MC_GROUP);
    }

    @Test
    public void register() throws Exception {
        assertEquals(ErrorCode.FAIL, client1.register("user1", ""));
        assertEquals(ErrorCode.FAIL, client1.register("user2", "123456"));
        assertEquals(ErrorCode.FAIL, client1.register("user3", "qwerty"));
        assertEquals(ErrorCode.FAIL, client1.register("user4", ""));
        assertEquals(ErrorCode.FAIL, client1.register("Al", "123456"));
        assertEquals(ErrorCode.FAIL, client1.register("Alina", "123"));
        assertEquals(ErrorCode.FAIL, client1.register("Bob", ""));
        assertEquals(ErrorCode.SUCCESS, client1.register("user", "123456"));
        assertEquals(ErrorCode.SUCCESS, client1.logout());
    }

    @Test
    public void login() throws Exception {
        assertEquals(ErrorCode.SUCCESS, client1.login("user1", "123456"));
        assertEquals(ErrorCode.SUCCESS, client1.logout());
        assertEquals(ErrorCode.WRONG_PASSWORD, client1.login("user2", ""));
        assertEquals(ErrorCode.SUCCESS, client1.login("user3", "123456"));
        assertEquals(ErrorCode.SUCCESS, client1.logout());
        assertEquals(ErrorCode.USER_NOT_REGISTERED, client1.login("Alina", "123456"));
    }

    @Test
    public void addFriend() throws Exception {
        assertEquals(ErrorCode.USER_NOT_LOGGED, client1.addFriend("user4"));
        assertEquals(ErrorCode.SUCCESS, client1.login("user2", "123456"));
        assertEquals(ErrorCode.USER_ALREADY_FRIEND, client1.addFriend("user1"));
        assertEquals(ErrorCode.TARGET_USER_DOES_NOT_EXIST, client1.addFriend("Bob"));
        assertEquals(ErrorCode.SUCCESS, client1.logout());
        assertEquals(ErrorCode.SUCCESS, client1.login("user3", "123456"));
        assertEquals(ErrorCode.REQUEST_ALREADY_SENT, client1.addFriend("user2"));
        assertEquals(ErrorCode.REQUEST_ALREADY_RECEIVED, client1.addFriend("user4"));
        assertEquals(ErrorCode.TARGET_USER_IS_NOT_ONLINE, client1.addFriend("user7"));
        assertEquals(ErrorCode.SUCCESS, client2.login("user7", "123456"));
        assertEquals(ErrorCode.SUCCESS, client1.addFriend("user7"));
        Thread.sleep(1000);
        assertTrue(client2.getRequests().contains("user3"));
        assertEquals(ErrorCode.SUCCESS, client1.logout());
        assertEquals(ErrorCode.SUCCESS, client2.logout());
    }

    @Test
    public void confirmFriendship() throws Exception {
        assertEquals(ErrorCode.USER_NOT_LOGGED, client1.confirmFriendship("user4"));
        assertEquals(ErrorCode.SUCCESS, client1.login("user3", "123456"));
        User.setFriendRequestDuration(0);
        assertEquals(ErrorCode.REQUEST_CANNOT_BE_ACCEPTED, client1.confirmFriendship("user4"));
        assertEquals(ErrorCode.SUCCESS, client2.login("user4", "123456"));
        User.setFriendRequestDuration("PT24M");
        assertEquals(ErrorCode.SUCCESS, client2.addFriend("user3"));
        assertEquals(ErrorCode.SUCCESS, client1.confirmFriendship("user4"));
        assertEquals(ErrorCode.TARGET_USER_DOES_NOT_EXIST, client1.confirmFriendship("Bob"));
        assertEquals(ErrorCode.REQUEST_CANNOT_BE_ACCEPTED, client1.confirmFriendship("user4"));
        assertEquals(ErrorCode.SUCCESS, client1.logout());
        assertEquals(ErrorCode.SUCCESS, client2.logout());
    }

    @Test
    public void getFriendsList() throws Exception {
        assertEquals(ErrorCode.SUCCESS, client1.login("user1", "123456"));

        Map<String, Boolean> friends = client1.getFriendsList();

        assertNotEquals(null, friends);
        assertEquals(2, friends.size());
        assertEquals(false, friends.get("user2"));
        assertEquals(false, friends.get("user3"));
        assertEquals(ErrorCode.SUCCESS, client1.login("user2", "123456"));

        friends = client1.getFriendsList();

        assertNotEquals(null, friends);
        assertEquals(1, friends.size());
        assertEquals(true, friends.get("user1"));
        assertEquals(ErrorCode.SUCCESS, client1.logout());
        assertEquals(ErrorCode.SUCCESS, client1.login("user1", "123456"));
        assertEquals(ErrorCode.SUCCESS, client1.logout());
        assertEquals(ErrorCode.SUCCESS, client1.login("user6", "123456"));

        friends = client1.getFriendsList();

        assertNotEquals(null, friends);
        assertEquals(2, friends.size());
        assertEquals(ErrorCode.SUCCESS, client1.logout());
        assertEquals(ErrorCode.SUCCESS, client1.login("user5", "123456"));

        friends = client1.getFriendsList();

        assertNotEquals(null, friends);
        assertTrue(friends.isEmpty());
        assertEquals(ErrorCode.SUCCESS, client1.logout());
    }

    @Test
    public void search() throws Exception {
        assertEquals(ErrorCode.SUCCESS, client1.login("user1", "123456"));

        List<String> users = client1.search("dummy");

        assertNotEquals(null, users);
        assertEquals(100, users.size());

        users = client1.search("user2");

        assertNotEquals(null, users);
        assertEquals(1, users.size());

        users = client1.search("nobody");

        assertNotEquals(null, users);
        assertEquals(0, users.size());
        assertEquals(ErrorCode.SUCCESS, client1.logout());
    }

    @Test
    public void post() throws Exception {
        assertEquals(ErrorCode.USER_NOT_LOGGED, client1.post("Hi!"));
        assertEquals(ErrorCode.SUCCESS, client1.login("user1", "123456"));
        assertEquals(ErrorCode.SUCCESS, client1.post("Ciao!"));
        assertEquals(ErrorCode.SUCCESS, client1.post("--------------------------------------------------" +
                "--------------------------------------------------" +
                "--------------------------------------------------" +
                "--------------------------------------------------" +
                "--------------------------------------------------" +
                "--------------------------------------------------" +
                "--------------------------------------------------" +
                "--------------------------------------------------" +
                "--------------------------------------------------" +
                "--------------------------------------------------" +
                "--------------------------------------------------" +
                "--------------------------------------------------" +
                "--------------------------------------------------" +
                "--------------------------------------------------"));
        assertEquals(ErrorCode.SUCCESS, client2.login("user2", "123456"));
        assertEquals(2, client2.getPosts().size());
        assertEquals(ErrorCode.SUCCESS, client1.post("Bye!"));
        Thread.sleep(1000);
        assertEquals(3, client2.getPosts().size());
        assertEquals(ErrorCode.SUCCESS, client1.logout());
        assertEquals(ErrorCode.SUCCESS, client2.logout());
    }

    @Test
    public void logout() throws Exception {
        assertEquals(ErrorCode.SUCCESS, client1.login("user1", "123456"));
        assertEquals(ErrorCode.SUCCESS, client1.logout());
        assertEquals(ErrorCode.USER_NOT_LOGGED, client1.logout());
    }

    @Test
    public void posts() throws Exception {
        BlockingQueue<Post> test = new LinkedBlockingQueue<>();
        Post p1 = new Post("Alina", "Toodle-loo!");
        Post p2 = new Post("Alina", "Au revoir!");
        Post p3 = new Post("Alina", "Auf wiedersehen!");
        Post p4 = new Post("Alina", "Ciao!");

        test.add(p1);
        test.add(p2);
        Client.storePosts(test, "test_posts");
        test = new LinkedBlockingQueue<>();
        test.add(p3);
        test.add(p4);
        test = Client.loadPosts("test_posts");

        assertEquals(2, test.size());
        assertEquals(p1.toString(), test.poll().toString());
        assertEquals(p2.toString(), test.poll().toString());
    }

    @Test
    public void requests() throws Exception {
        CopyOnWriteArrayList<String> test = new CopyOnWriteArrayList<>();
        String s1 = "Alina";
        String s2 = "Bob";
        String s3 = "Catherine";
        String s4 = "Dylan";

        test.add(s1);
        test.add(s2);
        Client.storeRequests(test, "test_requests");
        test = new CopyOnWriteArrayList<>();
        test.add(s3);
        test.add(s4);
        test = Client.loadRequests("test_requests");

        assertTrue(test.contains(s1));
        assertTrue(test.contains(s2));
        assertFalse(test.contains(s3));
        assertFalse(test.contains(s4));
    }

    @Test
    public void follow() throws Exception {
        assertEquals(ErrorCode.USER_NOT_LOGGED, client1.follow("user1"));
        assertEquals(ErrorCode.SUCCESS, client1.login("user8", "123456"));
        assertEquals(ErrorCode.SUCCESS, client2.login("user3", "123456"));
        assertEquals(ErrorCode.SUCCESS, client1.post("Ciao!"));
        Thread.sleep(1000);
        assertEquals(0, client2.getPosts().size());
        assertEquals(ErrorCode.TARGET_USER_DOES_NOT_EXIST, client2.follow("nobody"));
        assertEquals(ErrorCode.SUCCESS, client2.follow("user8"));
        assertEquals(ErrorCode.SUCCESS, client1.post("Ciao!"));
        Thread.sleep(1000);
        assertEquals(1, client2.getPosts().size());
        assertEquals(ErrorCode.SUCCESS, client1.logout());
        assertEquals(ErrorCode.SUCCESS, client2.logout());
    }
}
