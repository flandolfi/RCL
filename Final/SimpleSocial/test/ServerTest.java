// Francesco Landolfi
// Matr. 444151
// fran.landofli@gmail.com

import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

import org.junit.rules.TemporaryFolder;
import simplesocial.server.Server;
import simplesocial.server.UsersDB;

import java.util.Map;

import static org.junit.Assert.*;

public class ServerTest {
    @ClassRule
    public static TemporaryFolder temp = new TemporaryFolder();

    @BeforeClass
    public static void setUp() throws Exception {
        Server.DATA_DIR_PATH = temp.getRoot().getPath();
    }

    @Test
    public void parseArguments() throws Exception {
        Map<String, Object> opts = Server.parseArguments(new String[] { "-t=10", "-u=20",
                "-m=30", "-r=40", "-k=50", "-g=60", "-L=70", "-R=80"});

        assertEquals(10L, opts.get("tcp_port"));
        assertEquals(20L, opts.get("udp_port"));
        assertEquals(30L, opts.get("mc_port"));
        assertEquals(40L, opts.get("rmi_port"));
        assertEquals(50L, opts.get("keep_alive_millis"));
        assertEquals("60", opts.get("mc_group"));
        assertEquals("70", opts.get("log_duration"));
        assertEquals("80", opts.get("request_duration"));

        opts = Server.parseArguments(new String[] { "--tcp-port=100", "--udp-port=200", "--mc-port=300",
                "--rmi-port=400", "--keep-alive=500", "--mc-group=600", "--log-duration=700",
                "--req-duration=800"});

        assertEquals(100L, opts.get("tcp_port"));
        assertEquals(200L, opts.get("udp_port"));
        assertEquals(300L, opts.get("mc_port"));
        assertEquals(400L, opts.get("rmi_port"));
        assertEquals(500L, opts.get("keep_alive_millis"));
        assertEquals("600", opts.get("mc_group"));
        assertEquals("700", opts.get("log_duration"));
        assertEquals("800", opts.get("request_duration"));

        opts = Server.parseArguments(new String[] { "--tcp-port=abc", "--udp-port=20F", "--mc-port=XYZ",
                "--rmi-port={}", "--keep-alive=", "200", "XYZ ABC", "--t=2000", "-udp-port=5000" });

        assertEquals(null, opts.get("tcp_port"));
        assertEquals(null, opts.get("udp_port"));
        assertEquals(null, opts.get("mc_port"));
        assertEquals(null, opts.get("rmi_port"));
        assertEquals(null, opts.get("keep_alive_millis"));
        assertEquals(null, opts.get("mc_group"));
        assertEquals(null, opts.get("log_duration"));
        assertEquals(null, opts.get("request_duration"));
    }

    @Test
    public void configuration() throws Exception {
        Server.storeConfiguration();

        Map<String, Object> opts = Server.loadConfiguration();

        assertEquals(2000L, opts.get("tcp_port"));
        assertEquals(2500L, opts.get("udp_port"));
        assertEquals(3000L, opts.get("mc_port"));
        assertEquals(3500L, opts.get("rmi_port"));
        assertEquals(10000L, opts.get("keep_alive_millis"));
        assertEquals("239.255.1.1", opts.get("mc_group"));
        assertEquals("PT24M", opts.get("log_duration"));
        assertEquals("PT24M", opts.get("request_duration"));
    }

    @Test
    public void usersDB() throws Exception {
        UsersDB usersDB = new UsersDB();

        usersDB.register("JohnDoe", "123456");
        assertTrue(usersDB.isRegistered("JohnDoe"));

        Server.storeUsersDB(usersDB);

        usersDB = new UsersDB();
        usersDB.register("Alina", "qwerty");
        assertTrue(usersDB.isRegistered("Alina"));
        assertFalse(usersDB.isRegistered("JohnDoe"));

        usersDB = Server.loadUsersDB();

        assertTrue(usersDB.isRegistered("JohnDoe"));
        assertFalse(usersDB.isRegistered("Alina"));
    }
}
