// Francesco Landolfi
// Matr. 444151
// fran.landofli@gmail.com

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.LinkedList;

// ChatServer class
public class ChatServer {
    public final static int BLOCK_SIZE = 512;
    public final static int TCP_PORT = 2000;
    public final static int MC_PORT = 3000;
    public final static String MC_GROUP = "239.255.1.1";

    // Main
    public static void main(String[] args) {

        // Create & configure servers
        try (ServerSocketChannel TCPServer = ServerSocketChannel.open();
             DatagramChannel MCServer = DatagramChannel.open()) {
            SocketAddress group = new InetSocketAddress(InetAddress.getByName(ChatServer.MC_GROUP),
                    ChatServer.MC_PORT);
            MCServer.bind(null);
            MCServer.connect(group);
            TCPServer.bind(new InetSocketAddress(InetAddress.getLocalHost(), ChatServer.TCP_PORT));
            Selector selector = Selector.open();
            MCServer.configureBlocking(false);
            MCServer.register(selector, 0);
            TCPServer.configureBlocking(false);
            TCPServer.register(selector, SelectionKey.OP_ACCEPT);
            LinkedList<ByteBuffer> messages = new LinkedList<>();
            System.out.println("Server just started");

            while (true) {

                // Channel selection
                selector.selectedKeys().clear();
                selector.select();

                for (SelectionKey key: selector.selectedKeys()) {

                    // New TCP Client
                    if (key.isAcceptable()) {
                        try {
                            SocketChannel client = ((ServerSocketChannel) key.channel()).accept();
                            client.configureBlocking(false);
                            client.register(selector, SelectionKey.OP_READ);
                            System.out.println("New client accepted.");
                        } catch (IOException e){
                            System.err.println("Error accepting client: " + e.getMessage());
                            key.cancel();
                        }
                    }

                    // Forward received message from a client to all connected clients (multicast)
                    if (key.isWritable()) {
                        try {
                            DatagramChannel server = (DatagramChannel) key.channel();
                            ByteBuffer buffer = messages.poll();

                            if (buffer != null) {
                                server.write(buffer);
                                System.out.println("Forwarded: " + new String(buffer.array()));
                            }

                            if (messages.isEmpty()){
                                server.register(selector, 0);
                            } else {
                                server.register(selector, SelectionKey.OP_WRITE);
                            }
                        } catch (IOException e) {
                            System.err.println("Error writing to clients: " + e.getMessage());
                            key.cancel();
                        }
                    }

                    // Receive a message from a client (via TCP connection)
                    if (key.isReadable()) {
                        try {
                            SocketChannel client = (SocketChannel) key.channel();
                            ByteBuffer buffer = ByteBuffer.allocate(ChatServer.BLOCK_SIZE);

                            if (client.read(buffer) == -1) {
                                client.close();
                            } else {
                                System.out.println("Received: " + new String(buffer.array()));
                                buffer.flip();
                                messages.add(buffer);
                                MCServer.register(selector, SelectionKey.OP_WRITE);
                            }
                        } catch (IOException e) {
                            System.err.println("Error reading from client: " + e.getMessage());
                            key.cancel();
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
