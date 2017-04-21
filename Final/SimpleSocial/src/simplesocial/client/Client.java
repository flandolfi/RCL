// Francesco Landolfi
// Matr. 444151
// fran.landofli@gmail.com

package simplesocial.client;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import simplesocial.*;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.rmi.NoSuchObjectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * This class represent a client for the <b>SimpleSocial</b> social network.
 * <p>
 * The methods that interacts with the <b>SimpleSocial</b> server (i.e.
 * {@link #login(String, String) login}, {@link #register(String, String) register},
 * {@link #post(String) post}, {@link #addFriend(String) addFriend},
 * {@link #confirmFriendship(String) confirmFriendship},
 * {@link #getFriendsList() getFriendsList}, {@link #follow(String) follow},
 * {@link #search(String) search}, {@link #logout() logout}) may be also called
 * through a textual user interface using the {@link #main(String[]) main} method.
 *
 * @author Francesco Landolfi
 */
public class Client {
    /** The size of the {@link ByteBuffer} used for communication.*/
    public static final int BLOCK_SIZE = 512;

    /** The data directory path.*/
    public static String DATA_DIR_PATH = "./client_data/";

    /** The TCP port of the server. */
    public int TCP_PORT = 2000;

    /** The UDP port of the server. */
    public int UDP_PORT = 2500;

    /** The multicast port of the server. */
    public int MC_PORT = 3000;

    /** The RMI port of the server. */
    public int RMI_PORT = 3500;

    /** The IP address of the server.*/
    public String SERVER_ADDR = "127.0.1.1";

    /** The multicast group address of the server.*/
    public String MC_GROUP = "239.255.1.1";

    /** The network interface used by the client.*/
    public String INTERFACE = "wlan0";
    private String username = "";
    private int oAuth = -1;
    private PostNotifier postNotifier;
    private Updater updater;
    private CopyOnWriteArrayList<String> requests;
    private BlockingQueue<Post> posts;
    private KeepAliveSignalHandler KASHandler;
    private NotificationHandler NHandler;

    /**
     * Loads and returns the posts database file of the user <code>username</code>.
     * It must be stored in the directory located in {@link #DATA_DIR_PATH}, with the name
     * "{@code posts-<username>.db}".
     * If the file is not found, returns a new {@link BlockingQueue} object.
     * If an error occurs, prints an error message.
     *
     * @param username the name of the {@link simplesocial.server.User User} who is the
     *                 recipient of the posts
     * @return a {@link BlockingQueue} object containing a queue of {@link Post}
     * objects
     *
     * @see #storePosts(BlockingQueue, String)
     */
    public static BlockingQueue<Post> loadPosts(String username) {
        BlockingQueue<Post> posts;

        try (ObjectInputStream in = new ObjectInputStream(
                new FileInputStream(DATA_DIR_PATH + "posts-" + username + ".db"))) {
            posts = (BlockingQueue<Post>) in.readObject();
        } catch (FileNotFoundException e) {
            System.err.println("Posts database file not found.");
            posts = new LinkedBlockingQueue<>();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error while opening posts database file: " + e.getMessage());
            posts = new LinkedBlockingQueue<>();
        }

        return posts;
    }

    /**
     * Stores the queue of posts <code>posts</code> in a file located in the directory
     * {@link #DATA_DIR_PATH}, with the name "{@code posts-<username>.db}".
     * If the file does not exist, a new file is created.
     * If an error occurs, prints an error message.
     * <p>
     * The resulting file may be reloaded with the method {@link #loadPosts(String)}.
     *
     * @param posts the queue of posts to be stored
     * @param username the the name of the {@link simplesocial.server.User User} who is
     *                 the recipient of the posts
     */
    public static void storePosts(BlockingQueue<Post> posts, String username) {
        File file = new File(DATA_DIR_PATH + "posts-" + username + ".db");

        if (file.getParentFile() != null)
            file.getParentFile().mkdirs();

        try (ObjectOutputStream out = new ObjectOutputStream(
                new FileOutputStream(file))) {
            out.writeObject(posts);
        } catch (IOException e) {
            System.err.println("Error while writing posts database file: " + e.getMessage());
        }
    }

    /**
     * Loads and returns the friend requests database file of the user <code>username</code>.
     * It must be stored in the directory located in {@link #DATA_DIR_PATH}, with the name
     * "{@code requests-<username>.db}".
     * If the file is not found, returns a new {@link CopyOnWriteArrayList} object.
     * If an error occurs, prints an error message.
     *
     * @param username the name of the {@link simplesocial.server.User User} who has received
     *                 the friend requests
     * @return a {@link CopyOnWriteArrayList} object containing a list of {@link String}
     * that represents the name of the {@link simplesocial.server.User User} that made a
     * friend request
     *
     * @see #addFriend(String)
     * @see #storeRequests(CopyOnWriteArrayList, String)
     */
    public static CopyOnWriteArrayList<String> loadRequests(String username) {
        CopyOnWriteArrayList<String> requests;

        try (ObjectInputStream in = new ObjectInputStream(
                new FileInputStream(DATA_DIR_PATH + "requests-" + username + ".db"))) {
            requests = (CopyOnWriteArrayList<String>) in.readObject();
        } catch (FileNotFoundException e) {
            System.err.println("Requests database file not found.");
            requests = new CopyOnWriteArrayList<>();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error while opening requests database file: " + e.getMessage());
            requests = new CopyOnWriteArrayList<>();
        }

        return requests;
    }

    /**
     * Stores the list of friend requests <code>requests</code> in a file located in the
     * directory {@link #DATA_DIR_PATH}, with the name "{@code requests-<username>.db}".
     * If the file does not exist, a new file is created.
     * If an error occurs, prints an error message.
     * <p>
     * The resulting file may be reloaded with the method {@link #loadRequests(String)}.
     *
     * @param requests the list of requests to be stored
     * @param username the name of the {@link simplesocial.server.User User} who has received
     *                 the friend requests
     */
    public static void storeRequests(CopyOnWriteArrayList<String> requests, String username) {
        File file = new File(DATA_DIR_PATH + "requests-" + username + ".db");

        if (file.getParentFile() != null)
            file.getParentFile().mkdirs();

        try (ObjectOutputStream out = new ObjectOutputStream(
                new FileOutputStream(file))) {
            out.writeObject(requests);
        } catch (IOException e) {
            System.err.println("Error while writing requests database file: " + e.getMessage());
        }
    }

    /**
     * Loads the configuration from a JSON file named "<code>conf.json</code>" located in
     * the directory {@link #DATA_DIR_PATH}.
     * <p>
     * The file must have the following attributes:
     * <ul>
     *     <li><code>server_address</code>: the IP of the server;</li>
     *     <li><code>mc_group</code>: the IP of the multicast group;</li>
     *     <li><code>tcp_port</code>: the TCP port of the server;</li>
     *     <li><code>udp_port</code>: the UDP port of the server;</li>
     *     <li><code>mc_port</code>: the multicast port of the server;</li>
     *     <li><code>rmi_port</code>: the RMI port of the server;</li>
     *     <li><code>network_interface</code>: the network interface used by
     *     the client.</li>
     * </ul>
     * <p>
     * This method will modify the values of {@link #TCP_PORT}, {@link #UDP_PORT},
     * {@link #MC_PORT}, {@link #RMI_PORT}, {@link #SERVER_ADDR}, {@link #MC_GROUP}
     * and {@link #INTERFACE}. If the file does not exist or an error occurs, the
     * client will use the default values.
     *
     * @see #storeConfiguration()
     */
    public void loadConfiguration() {
        try (BufferedReader file = new BufferedReader(
                new FileReader(DATA_DIR_PATH + "conf.json"))) {
            JSONParser parser = new JSONParser();
            JSONObject conf = (JSONObject) parser.parse(file);

            TCP_PORT = (int)(long) conf.get("tcp_port");
            UDP_PORT = (int)(long) conf.get("udp_port");
            MC_PORT = (int)(long) conf.get("mc_port");
            RMI_PORT = (int)(long) conf.get("rmi_port");
            SERVER_ADDR = (String) conf.get("server_address");
            MC_GROUP = (String) conf.get("mc_group");
            INTERFACE = (String) conf.get("network_interface");
        } catch (FileNotFoundException e) {
            System.err.println("Configuration file not found.");
            storeConfiguration();
        } catch (IOException e) {
            System.err.println("Error opening configuration file.");
        } catch (Exception e) {
            System.err.println("Malformed configuration file.");
        }
    }

    /**
     * Stores the configuration in a JSON file named "<code>conf.json</code>" located in
     * the directory {@link #DATA_DIR_PATH}.
     * <p>
     * This method will save the values of {@link #TCP_PORT}, {@link #UDP_PORT},
     * {@link #MC_PORT}, {@link #RMI_PORT}, {@link #SERVER_ADDR}, {@link #MC_GROUP}
     * and {@link #INTERFACE}.
     * <p>
     * The generated file may be reloaded using the method {@link #loadConfiguration()}
     */
    public void storeConfiguration() {
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
            conf.put("server_address", SERVER_ADDR);
            conf.put("network_interface", INTERFACE);
            conf.writeJSONString(out);
        } catch (Exception e) {
            System.err.println("Error writing configuration file.");
            e.printStackTrace();
        }
    }

    /**
     * Returns the list of friend requests ({@link String} objects representing
     * the name of the {@link simplesocial.server.User User} that made the request).
     *
     * @return a list of requests ({@link String}s)
     */
    public List<String> getRequests() {
        return requests;
    }

    /**
     * Returns a queue of {@link Post}, in order of arrival.
     *
     * @return a queue of {@link Post}
     */
    public BlockingQueue<Post> getPosts() {
        return posts;
    }

    /**
     * Returns the name of the {@link simplesocial.server.User User} currently
     * logged on the server. If no {@link simplesocial.server.User User} is logged,
     * returns an empty {@link String}.
     *
     * @return the name of the {@link simplesocial.server.User User} currently
     * logged on the server
     */
    public String getCurrentUsername() {
        return username;
    }

    /**
     * Returns <code>true</code> if a {@link simplesocial.server.User User} is logged,
     * <code>false</code> otherwise.
     *
     * @return <code>true</code> if logged, <code>false</code> otherwise
     */
    public boolean isLogged() {
        return oAuth >= 0;
    }

    /**
     * Registers and logs in a {@link simplesocial.server.User User} with <code>username</code>
     * and <code>password</code> to the <b>SimpleSocial</b> server. Then returns
     * an {@link ErrorCode}, which can have one of the following values:
     * <ul>
     *     <li>{@link ErrorCode#SUCCESS}: the user is now registered and logged
     *     to the server;</li>
     *     <li>{@link ErrorCode#CONNECTION_ERROR}: an error occurred while connecting
     *     to the server (may depends on the client!)</li>
     *     <li>{@link ErrorCode#FAIL}: the connections was successful, but the server
     *     encountered a problem while registering the user (malformed <code>username</code>
     *     and/or <code>password</code>; <code>username</code> already registered).</li>
     * </ul>
     * <p>
     * <b>Note</b>: The <code>username</code> must contain only alphanumerics, '.', '-' and '_', and
     * must be 3 to 50 characters long. Same for the <code>password</code>, but must be long at
     * least 6 characters.
     *
     * @param username name of the {@link simplesocial.server.User User} to be registered
     * @param password password associated with the new {@link simplesocial.server.User User} account
     * @return an {@link ErrorCode}
     */
    public ErrorCode register(String username, String password) {
        // Starts the handlers
        int port = startHandlers(username);

        if (port < 0) {
            // An error occurred
            return ErrorCode.CONNECTION_ERROR;
        }

        // Connects to the server through a SocketChannel
        try (SocketChannel socket = SocketChannel.open(
                new InetSocketAddress(SERVER_ADDR, TCP_PORT))) {
            ByteBuffer buffer = ByteBuffer.allocate(BLOCK_SIZE);

            // Fills the buffer with the operation (int), the length of the username,
            // the username, the length of the password, the password and the port
            // of the client's TCP server
            buffer.putInt(Operation.REGISTER.toInt()).putInt(username.getBytes().length)
                    .put(username.getBytes()).putInt(password.getBytes().length)
                    .put(password.getBytes()).putInt(port).flip();
            socket.write(buffer);
            buffer.clear();
            socket.read(buffer); // Reply from the server
            buffer.flip();
            oAuth = buffer.getInt();

            if (oAuth >= 0) {
                // Login ok
                this.username = username;

                if (!startRMI(username)) {
                    // RMI cannot be started, logout
                    logout();

                    return ErrorCode.CONNECTION_ERROR;
                }

                return ErrorCode.SUCCESS;
            } else {
                // Failed to login
                stopHandlers();

                return ErrorCode.getCodeFromInt(oAuth);
            }
        } catch (Exception e) {
            stopHandlers();

            return ErrorCode.CONNECTION_ERROR;
        }
    }

    /**
     * Logs in a {@link simplesocial.server.User User} given <code>username</code> and
     * <code>password</code> to the <b>SimpleSocial</b> server. Then returns an
     * {@link ErrorCode}, which can have one of the following values:
     * <ul>
     *     <li>{@link ErrorCode#SUCCESS}: the user is now logged to the server;</li>
     *     <li>{@link ErrorCode#USER_NOT_REGISTERED}: the given <code>username</code>
     *     is not registered in the server;</li>
     *     <li>{@link ErrorCode#WRONG_PASSWORD}: <code>username</code> exists, but
     *     the given <code>password</code> is wrong;</li>
     *     <li>{@link ErrorCode#CONNECTION_ERROR}: an error occurred while connecting
     *     to the server (may depends on the client!).</li>
     * </ul>
     *
     * @param username name of the {@link simplesocial.server.User User} to be logged
     * @param password password associated with the {@link simplesocial.server.User User}
     *                 account
     * @return an {@link ErrorCode}
     */
    public ErrorCode login(String username, String password) {
        // Starts the handlers
        int port = startHandlers(username);

        if (port < 0) {
            // An error occurred
            return ErrorCode.CONNECTION_ERROR;
        }

        // Connects to the server through a SocketChannel
        try (SocketChannel socket = SocketChannel.open(
                new InetSocketAddress(SERVER_ADDR, TCP_PORT))) {
            ByteBuffer buffer = ByteBuffer.allocate(BLOCK_SIZE);

            // Fills the buffer with the operation (int), the length of the username,
            // the username, the length of the password, the password and the port
            // of the client's TCP server
            buffer.putInt(Operation.LOGIN.toInt()).putInt(username.getBytes().length)
                    .put(username.getBytes()).putInt(password.getBytes().length)
                    .put(password.getBytes()).putInt(port).flip();
            socket.write(buffer);
            buffer.clear();
            socket.read(buffer); // Reply from the server
            buffer.flip();
            oAuth = buffer.getInt();

            if (oAuth >= 0) {
                // Login ok
                this.username = username;

                if (!startRMI(username)) {
                    // RMI cannot be started, logout
                    logout();

                    return ErrorCode.CONNECTION_ERROR;
                }

                return ErrorCode.SUCCESS;
            } else {
                // Failed to login
                stopHandlers();

                return ErrorCode.getCodeFromInt(oAuth);
            }
        } catch (Exception e) {
            stopHandlers();

            return ErrorCode.CONNECTION_ERROR;
        }
    }

    /**
     * Current logged {@link simplesocial.server.User User} sends a friend request to another
     * {@link simplesocial.server.User User} which name is <code>friend</code>. Returns an
     * {@link ErrorCode}, which can have one of the following values:
     * <ul>
     *     <li>{@link ErrorCode#SUCCESS}: the request is sent;</li>
     *     <li>{@link ErrorCode#USER_NOT_LOGGED}: current user is not logged to the
     *     server. Possible causes are:
     *     <ul>
     *         <li>{@link #login(String, String) login} method is not been called yet
     *         or has not returned an {@link ErrorCode#SUCCESS};</li>
     *         <li>current login is expired (see {@link simplesocial.server.User#requestDuration});
     *         </li>
     *         <li>the {@link KeepAliveSignalHandler} has not responded to a Keep-Alive
     *         signal from the server.</li>
     *     </ul></li>
     *     <li>{@link ErrorCode#TARGET_USER_DOES_NOT_EXIST}: the given <code>friend</code>
     *     name is not been found on the server's database;</li>
     *     <li>{@link ErrorCode#USER_ALREADY_FRIEND}: <code>friend</code> is already a
     *     friend of the logged {@link simplesocial.server.User User};</li>
     *     <li>{@link ErrorCode#REQUEST_ALREADY_SENT}: logged {@link simplesocial.server.User User}
     *     already sent a request to <code>friend</code>;</li>
     *     <li>{@link ErrorCode#REQUEST_ALREADY_RECEIVED}: <code>friend</code> has already
     *     sent a friend request to logged {@link simplesocial.server.User User};</li>
     *     <li>{@link ErrorCode#TARGET_USER_IS_NOT_ONLINE}: <code>friend</code> is not
     *     online;</li>
     *     <li>{@link ErrorCode#REQUEST_CANNOT_BE_SENT}: the server encountered a problem
     *     saving the request;</li>
     *     <li>{@link ErrorCode#CONNECTION_ERROR}: an error occurred while connecting
     *     to the server (may depends on the client!).</li>
     * </ul>
     *
     * @param friend the name of the friend to add
     * @return an {@link ErrorCode}
     */
    public ErrorCode addFriend(String friend) {
        if (oAuth < 0)
            return ErrorCode.USER_NOT_LOGGED;

        // Connects to the server through a SocketChannel
        try (SocketChannel socket = SocketChannel.open(
                new InetSocketAddress(SERVER_ADDR, TCP_PORT))) {
            ByteBuffer buffer = ByteBuffer.allocate(BLOCK_SIZE);

            // Fills the buffer with the operation (int), the length of the username,
            // the username, the oAuth, the length of the friend's name and the
            // friend's name
            buffer.putInt(Operation.ADD_FRIEND.toInt()).putInt(username.getBytes().length)
                    .put(username.getBytes()).putInt(oAuth).putInt(friend.getBytes().length)
                    .put(friend.getBytes()).flip();
            socket.write(buffer);
            buffer.clear();
            socket.read(buffer); // Reply from the server
            buffer.flip();

            return ErrorCode.getCodeFromInt(buffer.getInt());
        } catch (Exception e) {
            return ErrorCode.CONNECTION_ERROR;
        }
    }

    /**
     * Confirms a friend request from {@link simplesocial.server.User User} <code>friend</code>.
     * You can see pending requests calling the method {@link #getRequests()}. Note that not all
     * the requests can be confirmed: some of them may be expired (see
     * {@link simplesocial.server.OnlineUsers#logDuration}). Returns an {@link ErrorCode}, which
     * can have one of the following values:
     * <ul>
     *     <li>{@link ErrorCode#SUCCESS}: logged {@link simplesocial.server.User User} is now
     *     friend with <code>friend</code> (the request is removed from the database);</li>
     *     <li>{@link ErrorCode#USER_NOT_LOGGED}: current user is not logged to the
     *     server. Possible causes are:
     *     <ul>
     *         <li>{@link #login(String, String) login} method is not been called yet
     *         or has not returned an {@link ErrorCode#SUCCESS};</li>
     *         <li>current login is expired (see {@link simplesocial.server.User#requestDuration});
     *         </li>
     *         <li>the {@link KeepAliveSignalHandler} has not responded to a Keep-Alive
     *         signal from the server.</li>
     *     </ul></li>
     *     <li>{@link ErrorCode#TARGET_USER_DOES_NOT_EXIST}: the given <code>friend</code>
     *     name is not been found on the server's database;</li>
     *     <li>{@link ErrorCode#REQUEST_CANNOT_BE_ACCEPTED}: the server encountered a problem
     *     accepting the request. Possible causes are:
     *     <ul>
     *         <li>logged {@link simplesocial.server.User User} is trying to accept a request that has
     *         not already received;</li>
     *         <li>selected request is expired (in this case, the request is removed from
     *         the database).</li>
     *     </ul></li>
     *     <li>{@link ErrorCode#CONNECTION_ERROR}: an error occurred while connecting
     *     to the server (may depends on the client!).</li>
     * </ul>
     *
     * @param friend the name of the friend to accept
     * @return an {@link ErrorCode}
     */
    public ErrorCode confirmFriendship(String friend) {
        if (oAuth < 0)
            return ErrorCode.USER_NOT_LOGGED;

        // Connects to the server through a SocketChannel
        try (SocketChannel socket = SocketChannel.open(
                new InetSocketAddress(SERVER_ADDR, TCP_PORT))) {
            ByteBuffer buffer = ByteBuffer.allocate(BLOCK_SIZE);

            // Fills the buffer with the operation (int), the length of the username,
            // the username, the oAuth, the length of the friend's name and the
            // friend's name
            buffer.putInt(Operation.CONFIRM_FRIENDSHIP.toInt()).putInt(username.getBytes().length)
                    .put(username.getBytes()).putInt(oAuth).putInt(friend.getBytes().length)
                    .put(friend.getBytes()).flip();
            socket.write(buffer);
            buffer.clear();
            socket.read(buffer); // Reply from the server
            buffer.flip();

            ErrorCode result = ErrorCode.getCodeFromInt(buffer.getInt());

            if (result == ErrorCode.SUCCESS || result == ErrorCode.REQUEST_CANNOT_BE_ACCEPTED)
                requests.remove(friend); // Remove the request from the database

            return result;
        } catch (Exception e) {
            return ErrorCode.CONNECTION_ERROR;
        }
    }

    /**
     * Follows a friend. In case of {@link ErrorCode#SUCCESS SUCCESS}, current
     * {@link simplesocial.server.User User} will receive updates from followed
     * {@link simplesocial.server.User User} (every post sent from him will be received and stored).
     * Returns an {@link ErrorCode}, which can have one of the following values:
     * <ul>
     *     <li>{@link ErrorCode#SUCCESS}: logged {@link simplesocial.server.User User} is now following
     *     <code>friend</code>;</li>
     *     <li>{@link ErrorCode#USER_NOT_LOGGED}: current user is not logged to the
     *     server. Possible causes are:
     *     <ul>
     *         <li>{@link #login(String, String) login} method is not been called yet
     *         or has not returned an {@link ErrorCode#SUCCESS};</li>
     *         <li>current login is expired (see {@link simplesocial.server.User#requestDuration});</li>
     *         <li>the {@link KeepAliveSignalHandler} has not responded to a Keep-Alive
     *         signal from the server.</li>
     *     </ul></li>
     *     <li>{@link ErrorCode#TARGET_USER_DOES_NOT_EXIST}: the given <code>friend</code>
     *     name is not been found on the server's database;</li>
     *     <li>{@link ErrorCode#FAIL}: the server encountered a problem. Possible causes are:
     *     <ul>
     *         <li>logged {@link simplesocial.server.User User} is not a friend of
     *         <code>friend</code>;</li>
     *         <li>logged {@link simplesocial.server.User User} is already a friend of
     *         <code>friend</code>;</li>
     *     </ul></li>
     *     <li>{@link ErrorCode#CONNECTION_ERROR}: an error occurred while connecting
     *     to the server (may depends on the client!).</li>
     * </ul>
     *
     * @param friend the name of the friend to follow
     * @return an {@link ErrorCode}
     */
    public ErrorCode follow(String friend) {
        try {
            if (updater == null) // RMI is not started yet
                return ErrorCode.USER_NOT_LOGGED;

            return updater.follow(friend, username, oAuth);
        } catch (RemoteException e) {
            return ErrorCode.CONNECTION_ERROR;
        }
    }

    /**
     * Retrieves current {@link simplesocial.server.User User}'s friends list from the server.
     * Returns an {@link HashMap}&lt;{@link String}, {@link Boolean}&gt;, where the {@link String}s
     * represent the the username of a friend and the {@link Boolean} his/her status
     * (<code>true</code> if online, <code>false</code> otherwise). If an error occurs,
     * returns <code>null</code>.
     *
     * @return a {@link Map} of {@link String} (username) and {@link Boolean} (online/offline)
     */
    public Map<String, Boolean> getFriendsList() {
        if (oAuth < 0)
            return null;

        // Connects to the server through a SocketChannel
        try (SocketChannel socket = SocketChannel.open(
                new InetSocketAddress(SERVER_ADDR, TCP_PORT))) {
            ByteBuffer buffer = ByteBuffer.allocate(BLOCK_SIZE);

            // Fills the buffer with the operation (int), the length of the username,
            // the username and the oAuth
            buffer.putInt(Operation.GET_FRIENDS_LIST.toInt()).putInt(username.getBytes().length)
                    .put(username.getBytes()).putInt(oAuth).flip();
            socket.write(buffer);
            buffer.clear();
            socket.read(buffer); // Reply from the server
            buffer.flip();
            int size = buffer.getInt(); // The number of friends on the list

            // For each friend, retrieves an int (number of bytes occupied by the username),
            // an array of bytes (the username in bytes) and another int (0 if online, -1 otherwise);
            // then fills an HashMap with the results.
            if (size >= 0) {
                HashMap<String, Boolean> result = new HashMap<>();
                byte[] name;

                for (int i = 0; i < size; i++) {
                    // If there are still friends to get but the buffer is drained, reads another buffer
                    if (buffer.remaining() == 0) {
                        buffer.clear();
                        socket.read(buffer);
                        buffer.flip();
                    }

                    name = new byte[buffer.getInt()];
                    buffer.get(name);
                    result.put(new String(name), buffer.getInt() == 0);
                }

                return result;
            }

            return null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Searches for {@link simplesocial.server.User User}s which username contains
     * <code>string</code>. Returns a {@link List} of {@link String}s containing the results
     * retrieved from the server. If an error occurs, return <code>null</code>.
     *
     * @param string the string to search
     * @return a {@link List} of usernames, or <code>null</code> in case of error
     */
    public List<String> search(String string) {
        if (oAuth < 0)
            return null;

        // Connects to the server through a SocketChannel
        try (SocketChannel socket = SocketChannel.open(
                new InetSocketAddress(SERVER_ADDR, TCP_PORT))) {
            ByteBuffer buffer = ByteBuffer.allocate(BLOCK_SIZE);

            // Fills the buffer with the operation (int), the length of the username,
            // the username, the oAuth, the length of the string and the string itself
            buffer.putInt(Operation.SEARCH.toInt()).putInt(username.getBytes().length)
                    .put(username.getBytes()).putInt(oAuth).putInt(string.getBytes().length)
                    .put(string.getBytes()).flip();
            socket.write(buffer);
            buffer.clear();
            socket.read(buffer); // Reply from the server
            buffer.flip();
            int size = buffer.getInt(); // The number of users on the list

            // For each user, retrieves an int (number of bytes occupied by the username) and
            // an array of bytes (the username in bytes); then fills an ArrayList with the results.
            if (size >= 0) {
                ArrayList<String> result = new ArrayList<>();
                byte[] name;

                for (int i = 0; i < size; i++) {
                    // If there are still users to get but the buffer is drained, reads another buffer
                    if (buffer.remaining() == 0) {
                        buffer.clear();
                        socket.read(buffer);
                        buffer.flip();
                    }

                    name = new byte[buffer.getInt()];
                    buffer.get(name);
                    result.add(new String(name));
                }

                return result;
            }

            return null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Submits a {@link Post}. Returns an {@link ErrorCode}, which can have one of
     * the following values:
     * <ul>
     *     <li>{@link ErrorCode#SUCCESS}: <code>post</code> is submitted as a new
     *     {@link Post} to the server;</li>
     *     <li>{@link ErrorCode#USER_NOT_LOGGED}: current user is not logged to the
     *     server. Possible causes are:
     *     <ul>
     *         <li>{@link #login(String, String) login} method is not been called yet
     *         or has not returned an {@link ErrorCode#SUCCESS};</li>
     *         <li>current login is expired (see {@link simplesocial.server.User#requestDuration});
     *         </li>
     *         <li>the {@link KeepAliveSignalHandler} has not responded to a Keep-Alive
     *         signal from the server.</li>
     *     </ul></li>
     *     <li>{@link ErrorCode#CONNECTION_ERROR}: an error occurred while connecting
     *     to the server (may depends on the client!).</li>
     * </ul>
     *
     * @param post the content of the {@link Post} to be submitted
     * @return an {@link ErrorCode}
     */
    public ErrorCode post(String post) {
        if (oAuth < 0)
            return ErrorCode.USER_NOT_LOGGED;

        // Connects to the server through a SocketChannel
        try (SocketChannel socket = SocketChannel.open(
                new InetSocketAddress(SERVER_ADDR, TCP_PORT))) {
            ByteBuffer buffer = ByteBuffer.allocate(BLOCK_SIZE);

            // Fills the buffer with the operation (int), the length of the username,
            // the username and the oAuth
            buffer.putInt(Operation.POST.toInt()).putInt(username.getBytes().length)
                    .put(username.getBytes()).putInt(oAuth).flip();
            socket.write(buffer);
            buffer.clear();
            socket.read(buffer); // Reply from the server
            buffer.flip();
            ErrorCode result = ErrorCode.getCodeFromInt(buffer.getInt());

            // If the authentication was successful, start sending the content of the post
            // (the post could be very long, so is sent only after the authentication)
            if (result == ErrorCode.SUCCESS) {
                int offset, remaining, length = buffer.remaining();

                // Fills the first buffer with the total length of the post in bytes (int),
                // then fills a new buffer until the full content is sent
                buffer.clear();
                buffer.putInt(post.getBytes().length);
                buffer.put(post.getBytes(), 0, length).flip();
                remaining = post.getBytes().length - length; // Number of bytes still to be sent
                offset = length;
                socket.write(buffer);

                while (remaining > 0) {
                    buffer.clear();
                    length = buffer.remaining() < remaining? buffer.remaining() : remaining;
                    buffer.put(post.getBytes(), offset, length).flip();
                    socket.write(buffer);
                    offset += length;
                    remaining -= length;
                }
            }

            return result;
        } catch (Exception e) {
            return ErrorCode.CONNECTION_ERROR;
        }
    }

    /**
     * Logs current {@link simplesocial.server.User User} out. Returns an {@link ErrorCode},
     * which can have one of the following values:
     * <ul>
     *     <li>{@link ErrorCode#SUCCESS}: current {@link simplesocial.server.User User} is
     *     now logged out;</li>
     *     <li>{@link ErrorCode#USER_NOT_LOGGED}: current user is not logged to the
     *     server. Possible causes are:
     *     <ul>
     *         <li>{@link #login(String, String) login} method is not been called yet
     *         or has not returned an {@link ErrorCode#SUCCESS};</li>
     *         <li>current login is expired (see {@link simplesocial.server.User#requestDuration});
     *         </li>
     *         <li>the {@link KeepAliveSignalHandler} has not responded to a Keep-Alive
     *         signal from the server.</li>
     *     </ul></li>
     *     <li>{@link ErrorCode#CONNECTION_ERROR}: an error occurred while connecting
     *     to the server (may depends on the client!).</li>
     * </ul>
     *
     * @return an {@link ErrorCode}
     */
    public ErrorCode logout() {
        if (oAuth < 0)
            return ErrorCode.USER_NOT_LOGGED;

        // Connects to the server through a SocketChannel
        try (SocketChannel socket = SocketChannel.open(
                new InetSocketAddress(SERVER_ADDR, TCP_PORT))) {
            ByteBuffer buffer = ByteBuffer.allocate(BLOCK_SIZE);

            // Fills the buffer with the operation (int), the length of the username,
            // the username and the oAuth
            buffer.putInt(Operation.LOGOUT.toInt()).putInt(username.getBytes().length)
                    .put(username.getBytes()).putInt(oAuth).flip();
            socket.write(buffer);
            buffer.clear();
            socket.read(buffer); // Reply from the server
            buffer.flip();
            ErrorCode result = ErrorCode.getCodeFromInt(buffer.getInt());

            if (result == ErrorCode.SUCCESS) {
                // Stops the handlers and the RMI
                stopHandlers();
                stopRMI();
            }

            resetStatus(); // Resets username to "" and oAuth to -1

            return result;
        } catch (Exception e) {
            return ErrorCode.CONNECTION_ERROR;
        }
    }

    // Starts RMI. Loads user's posts database; once exported the PostNotifier object
    // and located the registry, registers the user's callback.
    private boolean startRMI(String username) {
        try {
            posts = loadPosts(username);
            postNotifier = new PostNotifier(posts);
            UnicastRemoteObject.exportObject(postNotifier, 0);
            updater = (Updater) LocateRegistry.getRegistry(SERVER_ADDR, RMI_PORT)
                    .lookup(Updater.OBJECT_NAME);

            return updater.register(postNotifier, username, oAuth);
        } catch (RemoteException e) {
            System.err.println("RMI Error: " + e.getMessage());

            return false;
        } catch (NotBoundException e) {
            System.err.println("RMI Error: Class not found: " + e.getMessage());

            return false;
        }
    }

    // Stops RMI (unexports the callback postNotifier) and stores the posts database
    private boolean stopRMI() {
        try {
            storePosts(posts, username);
            UnicastRemoteObject.unexportObject(postNotifier, true);
        } catch (NoSuchObjectException e) {
            System.err.println("RMI Error: Could not unexport: " + e.getMessage());

            return false;
        }

        return true;
    }

    // Loads the requests database, starts the NotificationHandler and KeepAliveSignalHandler
    // threads. Returns the port on which the NotificationHandler thread (TCP server) is
    // listening. If an error occurs, stops the handlers and returns -1.
    private int startHandlers(String username) {
        int port;

        requests = loadRequests(DATA_DIR_PATH + "requests-" + username + ".db");
        NHandler = new NotificationHandler(this);
        (new Thread(NHandler)).start();
        port = NHandler.getLocalPort();

        if (port < 0) {
            NHandler.stop();

            return -1;
        }

        KASHandler = new KeepAliveSignalHandler(this);
        (new Thread(KASHandler)).start();

        return port;
    }

    // Stops the KeepAliveSignalHandler and NotificationHandler threads;
    // stores the requests database.
    private void stopHandlers() {
        storeRequests(requests, username);
        KASHandler.stop();
        NHandler.stop();
    }

    // Resets username and oAuth. Used mostly in case of CONNECTION_ERROR or
    // USER_NOT_LOGGED ErrorCode
    private void resetStatus() {
        username = "";
        oAuth = -1;
    }

    /**
     * Main method. Starts a textual user interface to interact with the
     * <b>SimpleSocial</b> server.
     *
     * @param args unused
     *
     * @see simplesocial.server.Server
     */
    public static void main(String[] args) {
        String username, password;
        Client client = new Client();

        // Loads last configuration from JSON file
        client.loadConfiguration();

        // Adds a cleanup function (works with SIGINT)
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                if (client.isLogged()) {
                    client.stopHandlers();
                    client.stopRMI();
                }
            }
        });

        try (BufferedReader in = new BufferedReader(new InputStreamReader(System.in))) {
            System.out.println("Welcome to SimpleSocial!\n------------------------");

            // Main loop
            while (true) {
                while (!client.isLogged()) {
                    System.out.print("\nChoose one of the following [1-4]:\n" +
                            "1. Login\n2. Register\n3. Settings\n4. Exit\n\n  > ");

                    // Reads user input
                    switch (in.readLine()) {
                        case "1": // Login
                            System.out.print("\nInsert username and password:\nUsername > ");
                            username = in.readLine();
                            System.out.print("Password > ");
                            password = in.readLine();

                            switch (client.login(username, password)) {
                                case SUCCESS:
                                    System.out.println("\nYou are now logged in!");
                                    break;

                                case CONNECTION_ERROR:
                                    System.out.println("\nConnection error! (try changing settings [3])");
                                    break;

                                case USER_NOT_REGISTERED:
                                case WRONG_PASSWORD:
                                    System.out.println("\nWrong username and/or password!");
                                    break;

                                default:
                                    System.out.println("\nAn error occurred!");
                                    break;
                            }

                            break;

                        case "2": // Register
                            System.out.println("\nInsert username and password (use only " +
                                    "alphanumerics, '.', '-', and '_'):");
                            System.out.print("Username (3-50 chars.) > ");
                            username = in.readLine();
                            System.out.print("Password (6-50 chars.) > ");
                            password = in.readLine();

                            switch (client.register(username, password)) {
                                case SUCCESS:
                                    System.out.println("\nYou are now registered and logged in!");
                                    break;

                                case CONNECTION_ERROR:
                                    System.out.println("\nConnection error! (try changing settings [3])");
                                    break;

                                default:
                                    System.out.println("\nWrong username and/or password, " +
                                            "or username already taken!");
                                    break;
                            }

                            break;

                        case "3": // Settings, modifies the client configuration
                            String input = "";

                            // Loops until an "8" (exit) is typed
                            while (!input.equals("8")) {
                                System.out.print("\nChoose one of the following [1-8]:\n" +
                                        "1. Set server IP address\n2. Set TCP port\n3. Set UDP port\n" +
                                        "4. Set Multicast group address\n5. Set Multicast port\n" +
                                        "6. Set RMI port\n7. Set Network Interface\n8. Exit\n\n  > ");

                                // Reads user input
                                switch (input = in.readLine()) {
                                    case "1": // IP
                                        System.out.print("\nInsert IP (actual: " +
                                                client.SERVER_ADDR + ") > ");
                                        client.SERVER_ADDR = in.readLine();
                                        break;

                                    case "2": // TCP port
                                        System.out.print("\nInsert TCP port (actual: " +
                                                client.TCP_PORT + ") > ");

                                        try {
                                            client.TCP_PORT = new Integer(in.readLine());
                                        } catch (Exception e) {
                                            System.out.println("\nThe TCP port must be a numeric value!");
                                        }

                                        break;

                                    case "3": // UDP port
                                        System.out.print("\nInsert UDP port (actual: " +
                                                client.UDP_PORT + ") > ");

                                        try {
                                            client.TCP_PORT = new Integer(in.readLine());
                                        } catch (Exception e) {
                                            System.out.println("\nThe UDP port must be a numeric value!");
                                        }

                                        break;

                                    case "4": // Multicast group address
                                        System.out.print("\nInsert Multicast group address " +
                                                "(actual: " + client.MC_GROUP + ") > ");

                                        client.MC_GROUP = in.readLine();

                                        break;

                                    case "5": // Multicast port
                                        System.out.print("\nInsert Multicast port (actual: " +
                                                client.MC_PORT + ") > ");

                                        try {
                                            client.MC_PORT = new Integer(in.readLine());
                                        } catch (Exception e) {
                                            System.out.println("\nThe Multicast port must be a numeric value!");
                                        }

                                        break;

                                    case "6": // RMI port
                                        System.out.print("\nInsert RMI port (actual: " +
                                                client.RMI_PORT + ") > ");

                                        try {
                                            client.RMI_PORT = new Integer(in.readLine());
                                        } catch (Exception e) {
                                            System.out.println("\nThe RMI port must be a numeric value!");
                                        }

                                        break;

                                    case "7": // Network interface
                                        System.out.print("\nInsert Network Interface (actual: " +
                                                client.INTERFACE + ") > ");

                                        client.INTERFACE = in.readLine();

                                        break;

                                    case "8": // Exit
                                        break;

                                    default:
                                        System.out.println("\nWrong option! (type 1-8)");
                                }

                                // Stores new configuration
                                client.storeConfiguration();
                            }

                            break;

                        case "4": // Exit
                            System.exit(0);
                            break;

                        default:
                            System.out.println("\nWrong option! (type 1-4)");
                            break;
                    }
                }

                // Login successful!

                // While user is logged, asks for actions
                while (client.isLogged()) {
                    System.out.print("\nChoose one of the following [1-9]:\n" +
                            "1. Show news feed\n2. Accept a friend request\n3. Add friend\n" +
                            "4. Search\n5. Show friends\n6. Post\n7. Follow a friend\n" +
                            "8. Logout\n9. Logout and Exit\n\n  > ");

                    // Reads user input
                    switch (in.readLine()) {
                        case "1": // Show news feed (locally)
                            System.out.println("\nNEWS:\n-----");

                            if (client.getPosts().isEmpty())
                                System.out.println("No post to show.");
                            else {
                                ArrayList<Post> list = new ArrayList<>();

                                client.getPosts().drainTo(list);
                                list.forEach(System.out::println);
                            }

                            break;

                        case "2": // Accept a friend request
                            // Shows pending friend requests first (locally)
                            System.out.println("\nREQUESTS:\n---------");

                            if (client.getRequests().isEmpty())
                                System.out.println("No pending request.");
                            else {
                                client.getRequests().forEach(System.out::println);
                                System.out.println("\nInsert the name of the friend you want to add:\n  > ");

                                // Calls the method after reading user's input
                                switch (client.confirmFriendship(in.readLine())) {
                                    case SUCCESS:
                                        System.out.println("\nYou are now friend with selected user!");
                                        break;

                                    case CONNECTION_ERROR:
                                        System.out.println("\nConnection error!");
                                        break;

                                    case USER_NOT_LOGGED:
                                        System.out.println("\nOuch.. Seems like you're no longer logged in!");
                                        client.resetStatus();
                                        break;

                                    case TARGET_USER_DOES_NOT_EXIST:
                                        System.out.println("\nSelected user does not exist!");
                                        break;

                                    case REQUEST_CANNOT_BE_ACCEPTED:
                                        System.out.println("\nRequest cannot be accepted!");
                                        break;
                                }
                            }

                            break;

                        case "3": // Add a friend
                            System.out.print("\nInsert friend's username:\n  > ");

                            // Calls the method after reading user's input
                            switch (client.addFriend(in.readLine())) {
                                case SUCCESS:
                                    System.out.println("\nRequest successfully sent!");
                                    break;

                                case USER_NOT_REGISTERED:
                                case USER_NOT_LOGGED:
                                    System.out.println("\nOuch.. Seems like you're no longer logged in!");
                                    client.resetStatus();
                                    break;

                                case TARGET_USER_DOES_NOT_EXIST:
                                    System.out.println("\nRequested user does not exist!");
                                    break;

                                case USER_ALREADY_FRIEND:
                                    System.out.println("\nRequested user is already your friend!");
                                    break;

                                case REQUEST_ALREADY_SENT:
                                    System.out.println("\nRequest is already been sent!");
                                    break;

                                case TARGET_USER_IS_NOT_ONLINE:
                                    System.out.println("\nRequested user is offline!");
                                    break;

                                case REQUEST_CANNOT_BE_SENT:
                                    System.out.println("\nRequest cannot be sent!");
                                    break;

                                case CONNECTION_ERROR:
                                    System.out.println("\nConnection error!");
                                    break;
                            }

                            break;

                        case "4": // Search
                            System.out.print("\nInsert a username (or part of it) to search:\n  > ");

                            // Calls the method after reading user's input
                            List<String> results = client.search(in.readLine());

                            if (results == null) {
                                System.out.println("\nOuch.. Seems like you're no longer logged in!");
                                client.resetStatus();
                            } else if (results.isEmpty())
                                System.out.println("\nNo user found.");
                            else {
                                // Show results
                                System.out.println("\nRESULTS:\n--------");
                                results.forEach(System.out::println);
                            }

                            break;

                        case "5": // Get friends list
                            // Calls the method after reading user's input
                            Map<String, Boolean> friends = client.getFriendsList();

                            if (friends == null) {
                                System.out.println("\nOuch.. Seems like you're no longer logged in!");
                                client.resetStatus();
                            } else if (friends.isEmpty())
                                System.out.println("\nNo user found.");
                            else {
                                // Show results
                                System.out.println("\nRESULTS:\n--------");
                                friends.forEach((String name, Boolean online) ->
                                        System.out.println(name + " (" + (online ? "online)" : "offline)")));
                            }

                            break;

                        case "6": // Post
                            System.out.print("\nType the content of the post:\n  > ");

                            // Calls the method after reading user's input
                            switch (client.post(in.readLine())) {
                                case SUCCESS:
                                    System.out.println("\nPost has been sent!");
                                    break;

                                case CONNECTION_ERROR:
                                    System.out.println("\nConnection error!");
                                    break;

                                case USER_NOT_REGISTERED:
                                case USER_NOT_LOGGED:
                                    System.out.println("\nOuch.. Seems like you're no longer logged in!");
                                    client.resetStatus();
                                    break;

                                default:
                                    System.out.println("\nAn error occurred!");
                                    break;
                            }

                            break;

                        case "7": // Follow
                            System.out.print("\nInsert the username of the friend to follow:\n  > ");

                            // Calls the method after reading user's input
                            switch (client.follow(in.readLine())) {
                                case SUCCESS:
                                    System.out.println("\nYou are now following requested user!");
                                    break;

                                case USER_NOT_LOGGED:
                                    System.out.println("\nOuch.. Seems like you're no longer logged in!");
                                    client.resetStatus();
                                    break;

                                case TARGET_USER_DOES_NOT_EXIST:
                                    System.out.println("\nRequested user does not exist!");
                                    break;

                                case CONNECTION_ERROR:
                                    System.out.println("\nConnection error!");
                                    break;

                                default:
                                    System.out.println("\nAn error occurred (you may not be friend " +
                                            "with requested user or you are already following him)");
                                    break;
                            }

                            break;

                        case "8": // Logout
                            switch (client.logout()) {
                                case SUCCESS:
                                    System.out.println("\nYou are now logged out.");
                                    break;

                                case CONNECTION_ERROR:
                                    System.out.println("\nConnection error!");
                                    break;

                                case USER_NOT_LOGGED:
                                    System.out.println("\nOuch.. Seems like you were already logged out!");
                                    client.resetStatus();
                                    break;
                            }

                            break;

                        case "9": // Logout & exit
                            switch (client.logout()) {
                                case SUCCESS:
                                    System.out.println("\nYou are now logged out.");
                                    System.exit(0);
                                    break;

                                case CONNECTION_ERROR:
                                    System.out.println("\nConnection error!");
                                    break;

                                case USER_NOT_LOGGED:
                                    System.out.println("\nOuch.. Seems like you were already logged out!");
                                    client.resetStatus();
                                    System.exit(0);
                                    break;
                            }

                            break;

                        default:
                            System.out.println("\nWrong option! (type 1-9)");
                            break;
                    }

                }
            }
        } catch(IOException e){
            System.err.println("An error occurred: " + e.getMessage());
        } finally {
            if (client.isLogged()) {
                client.stopHandlers();
                client.stopRMI();
            }
        }
    }
}
