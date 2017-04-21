// Francesco Landolfi
// Matr. 444151
// fran.landolfi@gmail.com

import java.io.*;
import java.net.*;
import java.nio.file.Paths;

// Client
public class MiniFTPClient {
    public static void main(String[] args) {
        // Create a new Socket
        try (Socket socket = new Socket()) {
            String path = args[0]; // Throws an exception if 'args[0]' is missing
            String filename = Paths.get(path).getFileName().toString();

            // Connect to the server
            System.out.println("Connecting to server...");
            socket.setSoTimeout(100000);
            socket.setTcpNoDelay(true);
            socket.connect(new InetSocketAddress(InetAddress.getLocalHost(), 1500));
            System.out.println("Connected; Requesting file \"" + path + "\"...");

            // Connected; the first message sent will be the path of the
            // requested file. Then the server will respond "OK, sending file..."
            // if the file is found, otherwise an error message.
            try (BufferedReader reader = new BufferedReader(
                     new InputStreamReader(socket.getInputStream()));
                 BufferedWriter writer = new BufferedWriter(
                     new OutputStreamWriter(socket.getOutputStream()))) {
                writer.write(path + "\n");
                writer.flush();
                String message = reader.readLine();
                System.out.println("Server replied: \"" + message + "\"");

                // The file has been found; start the transfer process...
                if (message.equals("OK, sending file...")) {
                    try (BufferedWriter file = new BufferedWriter(
                            new OutputStreamWriter(new FileOutputStream(filename)))) {
                        // Receive the file one line at a time
                        while ((message = reader.readLine()) != null)
                            file.write(message + "\n");

                        // File received
                        System.out.println("File received.");
                    }
                }
            }
        } catch (SocketTimeoutException e) {
            System.err.println("Connection timed out");
        } catch (SocketException e) {
            System.err.println("Server closed connection or an error occurred.");
        } catch (ArrayIndexOutOfBoundsException e) {
            System.err.println("Missing argument");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
