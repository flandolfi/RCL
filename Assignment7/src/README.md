Assignment - Week 7
===================

PING
----

Project file:

 - `PINGClient.java`;
 - `PINGServer.java`;
 - `PINGServerTask.java`.


Use the commands below to compile the project:

    $ javac PINGServer.java
    $ javac PINGClient.java

Run the server with the command

    $ java PINGServer [PORT] [SEED]

where `PORT` is the port number used by the server, and `SEED` is an optional
argument representing the initial seed of the random delay generator.

Then run the client with

    $ java PINGClient [NAME] [PORT]

where `NAME` and `PORT` are respectively the name and the port of the server.



