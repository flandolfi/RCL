// Francesco Landolfi
// Matr. 444151
// fran.landolfi@gmail.com

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

// Server
public class MiniFTPServer {
    public static void main(String[] args) {
        // Create a new ServerSocket
        try (ServerSocket server = new ServerSocket()) {
            server.bind(new InetSocketAddress(InetAddress.getLocalHost(), 1500));

            while (true) {
                System.out.println("Waiting for clients...");

                // Accept a new connection
                try (Socket client = server.accept();
                     BufferedReader reader = new BufferedReader(
                             new InputStreamReader(client.getInputStream()));
                     BufferedWriter writer = new BufferedWriter(
                             new OutputStreamWriter(client.getOutputStream()))) {
                    // Client connected; the first line received will be the
                    // path of the file requested
                    System.out.println("Client connected; Waiting for a file request...");
                    String message = reader.readLine();

                    // If file does not exist/is not a regular file, a
                    // FileNotFoundException is thrown
                    try (BufferedReader file = new BufferedReader(new InputStreamReader(
                            new FileInputStream(message)))) {
                        String line;

                        // Send a reply to the client, then the file will be transferred
                        System.out.println("\"" + message + "\" found, sending file...");
                        writer.write("OK, sending file...\n");

                        // Copy the file to the stream (one line at a time)
                        while ((line = file.readLine()) != null) {
                            writer.write(line + "\n");
                            writer.flush();
                        }
                    } catch (FileNotFoundException e) {
                        // If the file does not exist, send an error message to the client
                        System.out.println("\"" + message + "\" not found!");
                        writer.write("ERROR: File not found\n");
                    }
                } catch (IOException e) {
                    System.out.println("Client closed connection or some error appeared");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
