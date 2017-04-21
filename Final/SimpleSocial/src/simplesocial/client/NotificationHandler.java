// Francesco Landolfi
// Matr. 444151
// fran.landofli@gmail.com

package simplesocial.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

// NotificationHandler class. This class implements a thread that waits for friends
// requests for the user currently logged in to the SimpleSocial server
class NotificationHandler implements Runnable {
    private final Client client;
    private ServerSocket server;
    private final Semaphore semaphore;

    // Support class to handle a request. Every time a client sends a request to current
    // user, this thread creates an Handler thread and waits for another request
    private class Handler implements Runnable {
        private final Socket socket;

        // Handler constructor. Its argument is the socket used for the server-client
        // communication (note, this time the server *is* the client)
        public Handler(Socket socket) {
            this.socket = socket;
        }

        // Main thread. Gets the socket InputStream, reads the name of the user that made
        // the request and stores it in the requests database
        @Override
        public void run() {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()))) {
                client.getRequests().add(reader.readLine());
                System.out.println("New friend request received!");
            } catch (IOException e) {
                System.err.println("Error in NotificationHandler thread: " + e.getMessage());
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    // Ignored
                }
            }
        }
    }

    // NotificationHandler constructor. Its argument is the client currently logged to
    // the SimpleSocial server. It creates a new Semaphore initialized to zero. It represents
    // the availability of the port on which the new TCP server is bound.
    public NotificationHandler(Client client) {
        this.client = client;
        this.semaphore = new Semaphore(0);
    }

    // Main thread
    @Override
    public void run() {
        // The executor that manages the Handler threads
        ExecutorService ex = Executors.newCachedThreadPool();

        try {
            // Create a ServerSocket
            server = new ServerSocket();
            server.bind(new InetSocketAddress(InetAddress.getLocalHost(), 0));
            semaphore.release(); // The server is now bound

            // While the server is not closed or unbound, accepts new requests
            while (!server.isClosed() && server.isBound()) {
                try {
                    ex.submit(new Handler(server.accept()));
                } catch (IOException e) {
                    // Ignored
                }
            }
        } catch (IOException e) {
            System.err.println("Error in NotificationHandler thread: " + e.getMessage());
        } finally {
            // Closes the server, releases the semaphore and shuts the executor down

            try {
                server.close();
            } catch (Exception e) {
                // Ignored
            }

            semaphore.release();
            ex.shutdown();
        }
    }

    // Stops the NotificationHandler thread
    public void stop() {
        try {
            if (server != null && !server.isClosed())
                server.close();
        } catch (Exception e) {
            // Ignored
        }
    }

    // Gets the local port. This method blocks until the server is bound or closed or an error occurs
    // (in the late cases, returns -1)
    public int getLocalPort() {
        int port;

        try {
            semaphore.acquire();
            port = server != null && !server.isClosed() && server.isBound()? server.getLocalPort() : -1;
            semaphore.release();
        } catch (Exception e) {
            return -1;
        }

        return port;
    }
}
