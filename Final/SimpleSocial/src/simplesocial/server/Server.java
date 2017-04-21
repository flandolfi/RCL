// Francesco Landolfi
// Matr. 444151
// fran.landofli@gmail.com

package simplesocial.server;

import simplesocial.Updater;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This class represent the <b>SimpleSocial</b> server.
 *
 * @author Francesco Landolfi
 */
public class Server {
    /** The size of the {@link ByteBuffer} used for communication.*/
    public static final int BLOCK_SIZE = 512;

    /** The data directory path.*/
    public static String DATA_DIR_PATH = "./server_data/";
    private static int TCP_PORT = 2000;
    private static int UDP_PORT = 2500;
    private static int MC_PORT = 3000;
    private static int RMI_PORT = 3500;
    private static int KA_INTERVAL = 10000;
    private static String MC_GROUP = "239.255.1.1";
    private static int currentKAN = 0;
    private static UsersDB usersDB;

    // Singleton
    private Server() {}

    /**
     * This method parses the arguments from an array of {@link String}s.
     * Possible arguments are:
     * <ul>
     *     <li><code>-h</code>, or <code>--help</code>: shows online information about
     *     all available arguments and exits;</li>
     *     <li><code>-d</code>, or <code>--default</code>: for every non specified
     *     argument, uses default values;</li>
     *     <li><code>-s</code>, or <code>--save</code>: saves current configuration;</li>
     *     <li><code>-t=PORT</code>, or <code>--tcp-port=PORT</code>: sets <code>PORT</code>
     *     as TCP port;</li>
     *     <li><code>-u=PORT</code>, or <code>--udp-port=PORT</code>: sets <code>PORT</code>
     *     as UDP port;</li>
     *     <li><code>-m=PORT</code>, or <code>--mc-port=PORT</code>: sets <code>PORT</code>
     *     as multicast port;</li>
     *     <li><code>-r=PORT</code>, or <code>--rmi-port=PORT</code>: sets <code>PORT</code>
     *     as RMI port;</li>
     *     <li><code>-m=IP</code>, or <code>--mc-group=IP</code>: sets <code>IP</code> as
     *     multicast group address;</li>
     *     <li><code>-k=NUM</code>, or <code>--keep-alive=NUM</code>: sets <code>NUM</code>
     *     as Keep Alive interval (milliseconds);</li>
     *     <li><code>-L=PERIOD</code>, or <code>--log-duration=PERIOD</code>: sets
     *     <code>PERIOD</code> as log duration (see {@link OnlineUsers#setLogDuration(String)}).
     *     <code>PERIOD</code> must be in <code>PnDTnHnMn.nS</code> format (ISO-8601 standard);</li>
     *     <li><code>-R=PERIOD</code>, or <code>--request-duration=PERIOD</code>: sets
     *     <code>PERIOD</code> as request duration (see {@link User#setFriendRequestDuration(String)}).
     *     <code>PERIOD</code> must be in <code>PnDTnHnMn.nS</code> format (ISO-8601 standard);</li>
     * </ul>
     * All unrecognized arguments will be ignored.
     * <p>
     * Returns a {@link Map} of {@link String}s (the name of the argument) and {@link Object}s
     * (the value of the argument). This is the list of the possible argument names with their
     * relative value types:
     *
     * <table summary="" border="1" cellpadding="5" cellspacing="1">
     *     <tr>
     *         <th>Option</th>
     *         <th>Key</th>
     *         <th>Value Type</th>
     *     </tr>
     *     <tr>
     *         <td><code>-d</code>, <code>--default</code></td>
     *         <td><code>"default"</code></td>
     *         <td>{@link Boolean} (<code>true</code>)</td>
     *     </tr>
     *     <tr>
     *         <td><code>-s</code>, <code>--save</code></td>
     *         <td><code>"save"</code></td>
     *         <td>{@link Boolean} (<code>true</code>)</td>
     *     </tr>
     *     <tr>
     *         <td><code>-t</code>, <code>--tcp-port</code></td>
     *         <td><code>"tcp_port"</code></td>
     *         <td>{@link Long}</td>
     *     </tr>
     *     <tr>
     *         <td><code>-u</code>, <code>--udp-port</code></td>
     *         <td><code>"udp_port"</code></td>
     *         <td>{@link Long}</td>
     *     </tr>
     *     <tr>
     *         <td><code>-m</code>, <code>--mc-port</code></td>
     *         <td><code>"mc_port"</code></td>
     *         <td>{@link Long}</td>
     *     </tr>
     *     <tr>
     *         <td><code>-r</code>, <code>--rmi-port</code></td>
     *         <td><code>"rmi_port"</code></td>
     *         <td>{@link Long}</td>
     *     </tr>
     *     <tr>
     *         <td><code>-k</code>, <code>--keep-alive</code></td>
     *         <td><code>"keep_alive_millis"</code></td>
     *         <td>{@link Long}</td>
     *     </tr>
     *     <tr>
     *         <td><code>-m</code>, <code>--mc-group</code></td>
     *         <td><code>"mc_group"</code></td>
     *         <td>{@link String}</td>
     *     </tr>
     *     <tr>
     *         <td><code>-L</code>, <code>--log-duration</code></td>
     *         <td><code>"log_duration"</code></td>
     *         <td>{@link String}</td>
     *     </tr>
     *     <tr>
     *         <td><code>-R</code>, <code>--request-duration</code></td>
     *         <td><code>"request_duration"</code></td>
     *         <td>{@link String}</td>
     *     </tr>
     * </table>
     *
     * @param args the array of arguments
     * @return a {@link Map} of {@link String}s (the name of the argument) and {@link Object}s
     * (the value of the argument)
     */
    public static Map<String, Object> parseArguments(String[] args) {
        HashMap<String, Object> opts = new HashMap<>();

        for (String arg: args) {
            // Options with no arguments
            switch (arg) {
                case "-h":
                case "--help":
                    opts.put("help", true);
                    continue;
                case "-d":
                case "--default":
                    opts.put("default", true);
                    continue;
                case "-s":
                case "--save":
                    opts.put("save", true);
                    continue;
            }

            String[] tokens = arg.split("=");

            if (tokens.length != 2)
                continue;

            try {
                // Option with an argument after "=" (the split must return only 2 tokens)
                switch (tokens[0]) {
                    case "-t":
                    case "--tcp-port":
                        opts.put("tcp_port", new Long(tokens[1]));
                        break;
                    case "-u":
                    case "--udp-port":
                        opts.put("udp_port", new Long(tokens[1]));
                        break;
                    case "-m":
                    case "--mc-port":
                        opts.put("mc_port", new Long(tokens[1]));
                        break;
                    case "-r":
                    case "--rmi-port":
                        opts.put("rmi_port", new Long(tokens[1]));
                        break;
                    case "-k":
                    case "--keep-alive":
                        opts.put("keep_alive_millis", new Long(tokens[1]));
                        break;
                    case "-g":
                    case "--mc-group":
                        opts.put("mc_group", tokens[1]);
                        break;
                    case "-L":
                    case "--log-duration":
                        opts.put("log_duration", tokens[1]);
                        break;
                    case "-R":
                    case "--req-duration":
                        opts.put("request_duration", tokens[1]);
                        break;
                }
            } catch (Exception e) {
                System.err.println("Illegal argument at option " + tokens[0]);
            }
        }

        return opts;
    }

    /**
     * Loads the configuration from a JSON file named "<code>conf.json</code>" located in
     * the directory {@link #DATA_DIR_PATH}.
     * <p>
     * The file must have the following attributes:
     * <ul>
     *     <li><code>tcp_port</code>: the TCP port of the server;</li>
     *     <li><code>udp_port</code>: the UDP port of the server;</li>
     *     <li><code>mc_port</code>: the multicast port of the server;</li>
     *     <li><code>rmi_port</code>: the RMI port of the server;</li>
     *     <li><code>mc_group</code>: the IP of the multicast group;</li>
     *     <li><code>keep_alive_millis</code>: the interval in milliseconds between a Keep Alive
     *     signal and another;</li>
     *     <li><code>log_duration</code>: the duration of a login;</li>
     *     <li><code>request_duration</code>: the duration of a friend request.</li>
     * </ul>
     * <p>
     * Returns a {@link Map} of {@link String}s (the name of the attribute) and {@link Object}s
     * (the value of the attribute).
     *
     * @return a {@link Map} of {@link String}s (the name of the attribute) and {@link Object}s
     * (the value of the attribute)
     * @see #storeConfiguration()
     */
    public static Map<String, Object> loadConfiguration() {
        HashMap<String, Object> opts = new HashMap<>();

        try (BufferedReader file = new BufferedReader(
                new FileReader(DATA_DIR_PATH + "conf.json"))) {
            JSONParser parser = new JSONParser();
            JSONObject conf = (JSONObject) parser.parse(file);

            opts.putAll(conf);
        } catch (FileNotFoundException e) {
            System.err.println("Configuration file not found.");
        } catch (IOException e) {
            System.err.println("Error opening configuration file.");
        } catch (Exception e) {
            System.err.println("Malformed configuration file.");
        }

        return opts;
    }

    /**
     * Stores the configuration in a JSON file named "<code>conf.json</code>" located in
     * the directory {@link #DATA_DIR_PATH}.
     * <p>
     * The generated file may be reloaded using the method {@link #loadConfiguration()}
     */
    public static void storeConfiguration() {
        File file = new File(DATA_DIR_PATH + "conf.json");

        if (file.getParentFile() != null)
            file.getParentFile().mkdirs();

        try (BufferedWriter out = new BufferedWriter(
                new FileWriter(file))) {
            JSONObject conf = new JSONObject();

            conf.put("tcp_port", TCP_PORT);
            conf.put("udp_port", UDP_PORT);
            conf.put("mc_port", MC_PORT);
            conf.put("rmi_port", RMI_PORT);
            conf.put("mc_group", MC_GROUP);
            conf.put("keep_alive_millis", KA_INTERVAL);
            conf.put("log_duration", OnlineUsers.getLogDurationString());
            conf.put("request_duration", User.getFriendRequestDurationString());
            conf.writeJSONString(out);
        } catch (IOException e) {
            System.err.println("Error writing configuration file.");
        }
    }

    /**
     * Loads the users database from a file named "<code>users.db</code>" located in
     * the directory {@link #DATA_DIR_PATH}. If the file does not exist or an error occurs,
     * creates a new {@link UsersDB} instance.
     *
     * @return the {@link UsersDB} object found in the file, or a new one if it's not found
     */
    public static UsersDB loadUsersDB() {
        UsersDB usersDB;

        try (ObjectInputStream in = new ObjectInputStream(
                new FileInputStream(DATA_DIR_PATH + "users.db"))) {
            usersDB = (UsersDB) in.readObject();
        } catch (FileNotFoundException e) {
            System.err.println("Users database file not found.");
            usersDB = new UsersDB();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error while opening users database file: " + e.getMessage());
            usersDB = new UsersDB();
        }

        return usersDB;
    }

    /**
     * Stores the user database in a file named "<code>users.db</code>" located in
     * the directory {@link #DATA_DIR_PATH}.
     * <p>
     * The generated file may be reloaded using the method {@link #loadUsersDB()}
     *
     * @param usersDB the {@link UsersDB} object to be stored
     */
    public static void storeUsersDB(UsersDB usersDB) {
        File file = new File(DATA_DIR_PATH + "users.db");

        if (file.getParentFile() != null)
            file.getParentFile().mkdirs();

        try (ObjectOutputStream out = new ObjectOutputStream(
                new FileOutputStream(file))) {
            out.writeObject(usersDB);
        } catch (IOException e) {
            System.err.println("Error while writing users database file: " + e.getMessage());
        }
    }

    /**
     * Main method. Start a textual interface that will prompt any interaction with the various
     * <b>SimpleSocial</b> clients connected.
     *
     * @param args the server options (see {@link #parseArguments(String[])})
     */
    public static void main(String[] args) {
        Map<String, Object> opts = Server.parseArguments(args);

        if (opts.get("help") != null) {
            // If -h or --help are found, prints the online help and exit
            System.out.println("Usage server [OPTION]...\n" +
                    "Start a SimpleSocial server.\n\n" +
                    "  -h, --help                   display this help and exit\n" +
                    "  -t, --tcp-port=PORT          use PORT as TCP port\n" +
                    "  -u, --udp-port=PORT          use PORT as UDP port\n" +
                    "  -m, --mc-port=PORT           use PORT as multicast port\n" +
                    "  -r, --rmi-port=PORT          use PORT as RMI port\n" +
                    "  -k, --keep-alive=NUM         send a keep alive signal every NUM milliseconds\n" +
                    "  -g, --mc-group=IP            use IP as multicast address\n" +
                    "  -L, --log-duration=PERIOD    invalidate a log after PERIOD (using standard\n" +
                    "                               format PnDTnHnMn.nS)\n" +
                    "  -R, --req-duration=PERIOD    invalidate a friend request after PERIOD (using\n" +
                    "                               standard format PnDTnHnMn.nS)\n" +
                    "  -d, --default                use default configuration (for every\n" +
                    "                               non-specified value)\n" +
                    "  -s, --save                   save current configuration (will be auto-loaded\n" +
                    "                               from next start on)");
            System.exit(0);
        } else if (opts.get("default") == null) {
            // If the default option is not found, loads the configuration from file, then merges
            // the parsed options to the resulting configuration. The parsed ones will override
            // the old configuration
            Map<String, Object> conf = Server.loadConfiguration();

            conf.putAll(opts);
            opts = conf;
        }

        // Apply modifications, then stores them if the save option is found
        TCP_PORT = ((Long) opts.getOrDefault("tcp_port", (long) TCP_PORT)).intValue();
        UDP_PORT = ((Long) opts.getOrDefault("udp_port", (long) UDP_PORT)).intValue();
        MC_PORT = ((Long) opts.getOrDefault("mc_port", (long) MC_PORT)).intValue();
        RMI_PORT = ((Long) opts.getOrDefault("rmi_port", (long) RMI_PORT)).intValue();
        KA_INTERVAL = ((Long) opts.getOrDefault("keep_alive_millis", (long) KA_INTERVAL)).intValue();
        MC_GROUP = (String) opts.getOrDefault("mc_group", MC_GROUP);
        OnlineUsers.setLogDuration((String) opts.getOrDefault("log_duration",
                OnlineUsers.getLogDurationString()));
        User.setFriendRequestDuration((String) opts.getOrDefault("request_duration",
                User.getFriendRequestDurationString()));
        usersDB = Server.loadUsersDB();

        if (opts.get("save") != null) {
            Server.storeConfiguration();
        }

        ExecutorService keepAliveThreadEx = Executors.newSingleThreadExecutor();
        ExecutorService TCPThreadsEx = Executors.newCachedThreadPool();

        // Cleanup (works with SIGINT)
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                keepAliveThreadEx.shutdown();
                TCPThreadsEx.shutdown();
                Server.storeUsersDB(usersDB);
            }
        });

        // Opens, configures, and registers the various server channels to the selector
        try (ServerSocketChannel TCPServer = ServerSocketChannel.open();
             DatagramChannel MCServer = DatagramChannel.open();
             DatagramChannel UDPServer = DatagramChannel.open()) {
            OnlineUsers onlineUsers = new OnlineUsers();
            PostDispatcher postDispatcher = new PostDispatcher(onlineUsers, usersDB);
            Random random = new Random(System.currentTimeMillis());
            SocketAddress group = new InetSocketAddress(InetAddress.getByName(Server.MC_GROUP),
                    Server.MC_PORT);
            Updater updater = (Updater) UnicastRemoteObject.exportObject(postDispatcher, 0);
            Registry registry = LocateRegistry.createRegistry(RMI_PORT);
            ByteBuffer buffer = ByteBuffer.allocate(Server.BLOCK_SIZE);

            registry.rebind(Updater.OBJECT_NAME, updater);
            MCServer.bind(null);
            MCServer.connect(group);
            TCPServer.bind(new InetSocketAddress(InetAddress.getLocalHost(), Server.TCP_PORT));
            UDPServer.bind(new InetSocketAddress(InetAddress.getLocalHost(), Server.UDP_PORT));
            Selector selector = Selector.open();
            MCServer.configureBlocking(false);
            MCServer.register(selector, 0);
            TCPServer.configureBlocking(false);
            TCPServer.register(selector, SelectionKey.OP_ACCEPT);
            UDPServer.configureBlocking(false);
            UDPServer.register(selector, SelectionKey.OP_READ);

            // Starts a thread that will register the multicast channel to the selector every
            // KA_INTERVAL milliseconds to send a keep alive signal
            keepAliveThreadEx.execute(() -> {
                try {
                    while (true) {
                        Thread.sleep(KA_INTERVAL);
                        MCServer.register(selector, SelectionKey.OP_WRITE);
                        selector.wakeup();
                    }
                } catch (InterruptedException e) {
                    System.err.println("Keep Alive Thread interrupted.");
                } catch (ClosedChannelException e) {
                    System.err.println("Error in Keep Alive Thread: Multicast Channel is closed.");
                }
            });

            System.out.println("Server just started.");

            // Main loop
            while (true) {
                selector.selectedKeys().clear();
                selector.select();
                buffer.clear();

                for (SelectionKey key: selector.selectedKeys()) {
                    // Accepts TCP request
                    if (key.isAcceptable()) {
                        try {
                            // Accepts the client and leaves it to the TCPClientHandler.
                            // It will be used in blocking mode for a better protocol-based
                            // communication
                            TCPThreadsEx.submit(new TCPClientHandler(onlineUsers, usersDB,
                                    TCPServer.accept(), postDispatcher));
                            System.out.println("New TCP client accepted.");
                        } catch (IOException e) {
                            System.err.println("Error accepting client: " + e.getMessage());
                            key.cancel();
                        }
                    }

                    // Sends Keep Alive signal in multicast
                    if (key.isWritable()) {
                        try {
                            // Removes every user that has not responded to the old Keep Alive
                            // signal from the OnlineUsers database; then generates a random number
                            // that will be sent to the users as a Keep Alive signals; users have
                            // have to send it back to the server along with their usernames (via UDP)
                            onlineUsers.updateList().forEach(postDispatcher::unregister);
                            currentKAN = random.nextInt();
                            buffer.putInt(currentKAN).flip();
                            MCServer.write(buffer);
                            MCServer.register(selector, 0);
                            System.out.println("KEEP ALIVE signal sent.");
                        } catch (IOException e) {
                            System.err.println("Error writing to clients: " + e.getMessage());
                            key.cancel();
                        }
                    }

                    // Receives the Keep Alive replies from the clients via UDP
                    if (key.isReadable()) {
                        try {
                            // Receives the reply from a user to the Keep Alive number; it must contain
                            // the same number sent by the latest Keep Alive Signal and the username of
                            // the client. If the number is valid, the user will be marked as "alive"
                            UDPServer.receive(buffer);
                            buffer.flip();
                            int receivedKAN = buffer.getInt();
                            byte bytes[] = new byte[buffer.getInt()];
                            buffer.get(bytes);
                            String username = new String(bytes);

                            if (currentKAN == receivedKAN) {
                                onlineUsers.setAlive(username);
                                System.out.println("User \"" + username + "\" is alive :)");
                            } else {
                                System.err.println("Received wrong KEEP ALIVE NUMBER from user \"" +
                                        username + "\": received " + receivedKAN + " while current is" +
                                        currentKAN + ".");
                            }
                        } catch (IOException e) {
                            System.err.println("Error reading from client: " + e.getMessage());
                            key.cancel();
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}
