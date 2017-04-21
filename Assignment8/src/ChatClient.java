// Francesco Landolfi
// Matr. 444151
// fran.landofli@gmail.com

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// ChatClient class
public class ChatClient implements Runnable {
    private Selector selector;
    private SocketChannel socket;
    private boolean isRunning = true;

    // Constructor
    public ChatClient(Selector selector, SocketChannel socket) {
        this.selector = selector;
        this.socket = socket;
    }

    // Return the state of the reading thread
    public boolean isRunning() {
        return isRunning;
    }

    // Stop the reading thread
    public void stop() {
        this.isRunning = false;
    }

    // Reading thread - Reads a buffer from System.in and stores it in a "outgoing messages"
    // array list. Then the buffers will be sent in the main thread.
    @Override
    public void run() {
        ByteBuffer buffer = ByteBuffer.allocate(ChatServer.BLOCK_SIZE);
        LinkedList<Object> attachment = new LinkedList<>(); // Outgoing messages

        // Open the System.in channel
        try (ReadableByteChannel in = Channels.newChannel(System.in)) {
            System.out.println("Type a message and then press enter (type 'exit' to quit): ");
            in.read(buffer);

            // Loop until "exit" is typed or the thread is stopped
            while (this.isRunning &&
                    !"exit".equals((new String(buffer.array())).trim())) {
                buffer.position(buffer.position() - 1);
                buffer.put("#".getBytes()); // Appends '#'
                buffer.flip();
                attachment.add(buffer); // Store the buffer as attachment

                // Register the channel for a WRITE operation, then wake up the selector
                socket.register(selector, SelectionKey.OP_WRITE, attachment);
                selector.wakeup();

                // Allocate a new buffer and wait for a new input
                buffer = ByteBuffer.allocate(ChatServer.BLOCK_SIZE);
                in.read(buffer);
            }
        } catch (IOException e) {
            System.err.println("Thread error: " + e.getMessage());
        } finally {
            // Thread is stopping; Notify the main thread
            this.isRunning = false;
            socket.keyFor(selector).cancel();
            selector.wakeup();
            System.out.println("Stopped.");
        }
    }

    // Main
    public static void main(String[] args) {
        ExecutorService es = Executors.newSingleThreadExecutor();
        ChatClient service = null;

        // Create & configure connections to the server (both TCP and UDP/Multicast)
        try (SocketChannel client = SocketChannel.open(new InetSocketAddress(
                InetAddress.getLocalHost(), ChatServer.TCP_PORT));
             DatagramChannel channel = DatagramChannel.open(StandardProtocolFamily.INET)) {
            channel.setOption(StandardSocketOptions.SO_REUSEADDR, true);
            channel.bind(new InetSocketAddress(ChatServer.MC_PORT));
            channel.join(InetAddress.getByName(ChatServer.MC_GROUP),
                    NetworkInterface.getByName("wlan0")); // The only one that worked form me!

            // Open the selector and register the SocketChannel (InterestOps = 0) and
            // the DatagramChannel (InterestOps = OP_READ)
            Selector selector = Selector.open();
            channel.configureBlocking(false);
            client.configureBlocking(false);
            channel.register(selector, SelectionKey.OP_READ);
            client.register(selector, 0);
            service = new ChatClient(selector, client);
            es.execute(service); // Start the reading thread

            // While the reading thread is running, make channel selection
            while(service.isRunning()) {
                selector.selectedKeys().clear();
                selector.select();

                for (SelectionKey key: selector.selectedKeys()) {

                    // New message from Multicast
                    if (key.isReadable()) {
                        ByteBuffer buffer = ByteBuffer.allocate(ChatServer.BLOCK_SIZE);
                        DatagramChannel ch = (DatagramChannel) key.channel();
                        ch.receive(buffer);
                        System.out.println("Received: " + new String(buffer.array()));
                        buffer.clear();
                    }

                    // New Input from System.in (in attachment), forward it to the server
                    if (key.isWritable()) {
                        SocketChannel socket = (SocketChannel) key.channel();
                        LinkedList<Object> attachment = (LinkedList<Object>) key.attachment();
                        ByteBuffer buffer = (ByteBuffer) attachment.poll();

                        if (buffer != null) {
                            socket.write(buffer);
                            System.out.println("Sent: " + new String(buffer.array()));
                        }

                        if (attachment.isEmpty()) {
                            socket.register(selector, 0);
                        } else {
                            socket.register(selector, SelectionKey.OP_WRITE, attachment);
                        }
                    }
                }
            }
        } catch (IOException e) {
            // Can't interrupt the reading thread if stuck on a read()...
            System.err.println("Server disconnected or some error occurred, press 'Enter' to quit");
        } finally {
            if (service != null)
                service.stop(); // Stop the loop on the main thread

            es.shutdown();
        }
    }
}
