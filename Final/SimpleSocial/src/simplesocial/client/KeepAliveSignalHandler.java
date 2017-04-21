// Francesco Landolfi
// Matr. 444151
// fran.landofli@gmail.com

package simplesocial.client;

import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.DatagramChannel;

// KeepAliveSignalHandler class. This class implements the thread delegated to
// receive and reply to the keep-alive signals sent by the server on multicast.
class KeepAliveSignalHandler implements Runnable {
    private final Client client;
    private DatagramChannel in, out;
    private boolean isRunning = true;

    // Constructor. The passed client will be the one who responds to the signals
    public KeepAliveSignalHandler(Client client) {
        this.client = client;
    }

    // Main thread. Opens a channel that listens to the multicast group (in) and another one
    // that replies to the server (out)
    @Override
    public void run() {
        try {
            ByteBuffer buffer = ByteBuffer.allocate(Client.BLOCK_SIZE);
            InetSocketAddress address = new InetSocketAddress(client.SERVER_ADDR, client.UDP_PORT);

            in = DatagramChannel.open(StandardProtocolFamily.INET);
            out = DatagramChannel.open(StandardProtocolFamily.INET);
            in.setOption(StandardSocketOptions.SO_REUSEADDR, true);
            in.bind(new InetSocketAddress(client.MC_PORT));
            in.join(InetAddress.getByName(client.MC_GROUP),
                    NetworkInterface.getByName(client.INTERFACE));
            out.bind(null);

            // While is running, keeps waiting for a buffer from the multicast channel and
            // forwards it to the server, attaching client's current username length in bytes
            // (int) and the username itself
            while (isRunning) {
                in.receive(buffer);
                buffer.putInt(client.getCurrentUsername().getBytes().length)
                        .put(client.getCurrentUsername().getBytes()).flip();
                out.send(buffer, address);
                buffer.clear();
            }
        } catch (ClosedChannelException e) {
            // Ignored
        } catch (Exception e) {
            System.err.println("Error in KeepAliveSignalHandler thread: " + e.getMessage());
        } finally {
            try {
                // Close the channels
                in.close();
                out.close();
            } catch (Exception e) {
                // Ignored
            }
        }
    }

    // Stops the thread. If the channels were open, closes them.
    public void stop() {
        try {
            isRunning = false;

            if (in != null)
                in.close();

            if (out != null)
                out.close();
        } catch (Exception e) {
            // Ignored
        }
    }
}
