// Francesco Landolfi
// Matr. 444151
// fran.landofli@gmail.com

package simplesocial.server;

import simplesocial.ErrorCode;
import simplesocial.Operation;
import simplesocial.Post;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.List;

// TCPClientHandler class. This class represents the thread that will handle
// a client request via TCP
class TCPClientHandler implements Runnable {
    private final OnlineUsers onlineUsers;
    private final UsersDB usersDB;
    private final SocketChannel client;
    private final PostDispatcher postDispatcher;

    // Constructor
    public TCPClientHandler(OnlineUsers onlineUsers, UsersDB usersDB,
                            SocketChannel client, PostDispatcher postDispatcher) {
        this.onlineUsers = onlineUsers;
        this.usersDB = usersDB;
        this.client = client;
        this.postDispatcher = postDispatcher;
    }

    // Main thread. Parses the received buffer and execute the requested operation
    @Override
    public void run() {
        try {
            int oAuth, port;
            byte[] username, password, friendname;
            User user, friend;
            ByteBuffer buffer = ByteBuffer.allocate(Server.BLOCK_SIZE);

            client.configureBlocking(true); // Blocking mode
            client.read(buffer);
            buffer.flip();

            // Reads the operation to execute (the operations are self explanatory)
            switch (Operation.getOperationFromInt(buffer.getInt())) {
                // Register operation. Reads the username and the password, then registers and logs in
                // the user, and sends the oAuth back to the client (may be negative in case of errors)
                case REGISTER:
                    oAuth = ErrorCode.FAIL.toInt();
                    username = new byte[buffer.getInt()];
                    buffer.get(username);
                    password = new byte[buffer.getInt()];
                    buffer.get(password);
                    user = usersDB.register(new String(username), new String(password));
                    port = buffer.getInt();

                    if (user != null) {
                        oAuth = onlineUsers.login(user, new InetSocketAddress(
                                ((InetSocketAddress) client.getLocalAddress()).getAddress(), port));

                        if (oAuth >= 0)
                            System.out.println("New user registered: \"" + user.getUsername() + "\".");
                    }

                    buffer.clear();
                    buffer.putInt(oAuth).flip();
                    client.write(buffer);
                    break;

                // Login operation. Reads the username and the password, then logs in
                // the user and sends the oAuth back to the client (may be negative in case of errors)
                case LOGIN:
                    username = new byte[buffer.getInt()];
                    buffer.get(username);
                    password = new byte[buffer.getInt()];
                    buffer.get(password);
                    user = usersDB.getUserByName(new String(username));
                    port = buffer.getInt();

                    if (user == null)
                        oAuth = ErrorCode.USER_NOT_REGISTERED.toInt();
                    else if (user.checkPassword(new String(password))) {
                        oAuth = onlineUsers.login(user, new InetSocketAddress(
                                ((InetSocketAddress) client.getLocalAddress()).getAddress(), port));

                        if (oAuth >= 0)
                            System.out.println("User \"" + user.getUsername() + "\" is now logged in.");
                    } else
                        oAuth = ErrorCode.WRONG_PASSWORD.toInt();

                    buffer.clear();
                    buffer.putInt(oAuth).flip();
                    client.write(buffer);
                    break;

                // Add friend operation. Reads the username, the oAuth and the name of the friend to add.
                // Then, if no error occurs, creates a TCP sockets to the friend's client (using the
                // stored SockedAddress). Once the connection is created, the server sends the name of
                // the user to the friend and closes the connection. Then sends back to the user the result
                // of the operation (an ErrorCode converted to int)
                case ADD_FRIEND:
                    SocketAddress address;

                    username = new byte[buffer.getInt()];
                    buffer.get(username);
                    oAuth = buffer.getInt();
                    friendname = new byte[buffer.getInt()];
                    buffer.get(friendname);
                    user = onlineUsers.isLogged(new String(username), oAuth);
                    friend = usersDB.getUserByName(new String(friendname));
                    address = onlineUsers.getUserAddress(friend);
                    buffer.clear();

                    if (user == null)
                        buffer.putInt(ErrorCode.USER_NOT_LOGGED.toInt());
                    else if (friend == null)
                        buffer.putInt(ErrorCode.TARGET_USER_DOES_NOT_EXIST.toInt());
                    else if (friend.equals(user))
                        buffer.putInt(ErrorCode.REQUEST_CANNOT_BE_SENT.toInt());
                    else if (user.isFriend(friend))
                        buffer.putInt(ErrorCode.USER_ALREADY_FRIEND.toInt());
                    else if (friend.hasPendingRequestFrom(user))
                        buffer.putInt(ErrorCode.REQUEST_ALREADY_SENT.toInt());
                    else if (user.hasPendingRequestFrom(friend))
                        buffer.putInt(ErrorCode.REQUEST_ALREADY_RECEIVED.toInt());
                    else if (address == null)
                        buffer.putInt(ErrorCode.TARGET_USER_IS_NOT_ONLINE.toInt());
                    else {
                        Socket socket = new Socket();
                        BufferedWriter writer = null;

                        try {
                            socket.setSoTimeout(10000);
                            socket.setTcpNoDelay(true);
                            socket.connect(onlineUsers.getUserAddress(friend));
                            writer = new BufferedWriter(
                                    new OutputStreamWriter(socket.getOutputStream()));
                            writer.write(user.getUsername() + "\r\n");
                            writer.flush();

                            if (!friend.saveRequest(user))
                                buffer.putInt(ErrorCode.REQUEST_CANNOT_BE_SENT.toInt());
                            else {
                                buffer.putInt(ErrorCode.SUCCESS.toInt());
                                System.out.println("User \"" + user.getUsername() +
                                        "\" has sent a request to \"" + friend.getUsername() + "\".");
                            }

                        } catch(IOException e) {
                            buffer.putInt(ErrorCode.TARGET_USER_IS_NOT_ONLINE.toInt());
                        } finally {
                            try {
                                if (writer != null)
                                    writer.close();

                                socket.close();
                            } catch (IOException e) {
                                // Ignored
                            }
                        }
                    }

                    buffer.flip();
                    client.write(buffer);
                    break;

                // Confirm friendship operation. Reads the username, the oAuth and the name of the friend
                // request to confirm. Then sends back to the user the result of the operation (an ErrorCode
                // converted to int)
                case CONFIRM_FRIENDSHIP:
                    username = new byte[buffer.getInt()];
                    buffer.get(username);
                    oAuth = buffer.getInt();
                    friendname = new byte[buffer.getInt()];
                    buffer.get(friendname);
                    user = onlineUsers.isLogged(new String(username), oAuth);
                    friend = usersDB.getUserByName(new String(friendname));
                    buffer.clear();

                    if (user == null)
                        buffer.putInt(ErrorCode.USER_NOT_LOGGED.toInt());
                    else if (friend == null)
                        buffer.putInt(ErrorCode.TARGET_USER_DOES_NOT_EXIST.toInt());
                    else if (!user.acceptRequest(friend))
                        buffer.putInt(ErrorCode.REQUEST_CANNOT_BE_ACCEPTED.toInt());
                    else {
                        buffer.putInt(ErrorCode.SUCCESS.toInt());
                        System.out.println("User \"" + user.getUsername() +
                                "\" accepted the request from \"" + friend.getUsername() + "\".");
                    }

                    buffer.flip();
                    client.write(buffer);
                    break;

                // Get friend list operation. Reads the username and the oAuth, then sends just an ErrorCode
                // if an error occurs, else sends the number of the friends found (note that the size can
                // be greater or equal to zero, while the ErrorCode can be only negative - only SUCCESS is 0),
                // followed by all friends names (the length of the name followed by the name itself) along
                // with their statuses (online/offline). They may be sent in more than one buffer
                case GET_FRIENDS_LIST:
                    username = new byte[buffer.getInt()];
                    buffer.get(username);
                    oAuth = buffer.getInt();
                    user = onlineUsers.isLogged(new String(username), oAuth);
                    buffer.clear();

                    if (user == null)
                        buffer.putInt(ErrorCode.USER_NOT_LOGGED.toInt());
                    else {
                        List<String> friends = user.getFriendsList();
                        buffer.putInt(friends.size()); // How many usernames the server is sending

                        for (String name: friends) {
                            // If the buffer has not enough remaining space for the length of the name in
                            // bytes, the name in bytes and the status of the user, then sends this buffer
                            // and starts filling another one. The client will know there will be another
                            // buffer until it has received all its friends
                            if (buffer.remaining() < name.getBytes().length + Integer.BYTES*2) {
                                buffer.flip();
                                client.write(buffer);
                                buffer.clear();
                            }

                            buffer.putInt(name.getBytes().length);
                            buffer.put(name.getBytes());
                            buffer.putInt(onlineUsers.isAlive(name)? 0 : -1); // The status of the user
                        }
                    }

                    buffer.flip();
                    client.write(buffer);
                    break;

                // Search operation. Reads the username, the oAuth and a string to search, then sends just
                // an ErrorCode if an error occurs, else sends the number of the users found (note that
                // the size can be greater or equal to zero, while the ErrorCode can be only negative - only
                // SUCCESS is 0), followed by all users names (the length of the name followed by the name
                // itself). They may be sent in more than one buffer
                case SEARCH:
                    username = new byte[buffer.getInt()];
                    buffer.get(username);
                    oAuth = buffer.getInt();
                    friendname = new byte[buffer.getInt()];
                    buffer.get(friendname);
                    user = onlineUsers.isLogged(new String(username), oAuth);
                    buffer.clear();

                    if (user == null)
                        buffer.putInt(ErrorCode.USER_NOT_LOGGED.toInt());
                    else {
                        List<String> results = usersDB.search(new String(friendname));
                        buffer.putInt(results.size()); // How many usernames the server is sending

                        for (String name: results) {
                            // If the buffer has not enough remaining space for the length of the name in
                            // bytes, the name in bytes and the status of the user, then sends this buffer
                            // and starts filling another one. The client will know there will be another
                            // buffer until it has received all its friends
                            if (buffer.remaining() < name.getBytes().length + Integer.BYTES) {
                                buffer.flip();
                                client.write(buffer);
                                buffer.clear();
                            }

                            buffer.putInt(name.getBytes().length);
                            buffer.put(name.getBytes());
                        }
                    }

                    buffer.flip();
                    client.write(buffer);
                    break;

                // Post operation. Reads the username and the oAuth, then sends back an ErrorCode.
                // In case of success, receives firstly the length of the post, then the post
                // content split in all required buffers. Once the content is completely received,
                // it is sent to all the user's followers
                case POST:
                    username = new byte[buffer.getInt()];
                    buffer.get(username);
                    oAuth = buffer.getInt();
                    user = onlineUsers.isLogged(new String(username), oAuth);
                    buffer.clear();

                    if (user != null) {
                        buffer.putInt(ErrorCode.SUCCESS.toInt()).flip();
                        client.write(buffer);
                        buffer.clear();
                        client.read(buffer);
                        buffer.flip();

                        byte[] post = new byte[buffer.getInt()]; // An array of the post's length
                        int offset, remaining, length = buffer.remaining();

                        buffer.get(post, 0, length);
                        offset = length;
                        remaining = post.length - length;
                        buffer.clear();

                        // Until the array is filled, continues to receive buffers from the client
                        while(remaining > 0) {
                            client.read(buffer);
                            buffer.flip();
                            length = buffer.remaining();
                            buffer.get(post, offset, length);
                            offset += length;
                            remaining -= length;
                            buffer.clear();
                        }

                        postDispatcher.post(user, new String(post)); // Sends the post to the followers
                        System.out.println("New post from " + new String(post));
                    } else {
                        buffer.putInt(ErrorCode.USER_NOT_LOGGED.toInt()).flip();
                        client.write(buffer);
                    }

                    break;

                // Logout operation. Receives the username and the oAuth, then, if no error occurs,
                // logs out the user and unregisters it from the RMI server. Sends back an ErrorCode
                case LOGOUT:
                    username = new byte[buffer.getInt()];
                    buffer.get(username);
                    oAuth = buffer.getInt();
                    user = onlineUsers.isLogged(new String(username), oAuth);
                    buffer.clear();

                    if (user != null) {
                        onlineUsers.logout(user);
                        postDispatcher.unregister(new String(username));
                        buffer.putInt(ErrorCode.SUCCESS.toInt());
                        System.out.println("User \"" + user.getUsername() + "\" is now logged out.");
                    } else
                        buffer.putInt(ErrorCode.USER_NOT_LOGGED.toInt());

                    buffer.flip();
                    client.write(buffer);
                    break;
            }
        } catch (IOException e) {
            System.err.println("Error in client thread: " + e.getMessage());
        } finally {
            try {
                client.close();
            } catch (IOException e) {
                System.err.println("Error while closing the client channel.");
            }
        }
    }
}
