Assignment - Week 6
===================

OnlineShop
----------

Project file:

 - `Client.java`;
 - `ClientHandler.java`;
 - `data_1.json`;
 - `data_2.json`;
 - `data_3.json`;
 - `OnlineShop.java`;
 - `Product.java`;
 - `Server.java`;
 - `Updater.java`.

It is also required `json-simple.jar` as external library.


Use the commands below to compile the project:

    $ javac -classpath json-simple.jar:. *.java

then run any number of OnlineShop servers with the command

    $ java -classpath json-simple.jar:. OnlineShop [NAME] [PATH] [PORT]

where `NAME` is the given name of the seller/market, `PATH` is the location of
the products data (e.g. `data_1.json`, `data_2.json` or `data_3.json`) and PORT
is the port number used by the OnlineShop server (note: port 1500 is used by the
`Server` class).

Run the collector server with

    $ java Server [PORT LIST]

where `PORT LIST` is a list of ports of the active OnlineShop servers.

Finally, run any number of Client clients, with the command

    $ java Client


Example
-------

On terminal #1, type:

    $ java -classpath json-simple.jar:. OnlineShop Market1 data_1.json 1600

On terminal #2:

    $ java -classpath json-simple.jar:. OnlineShop Market2 data_2.json 1700

On terminal #3:

    $ java -classpath json-simple.jar:. OnlineShop Market3 data_3.json 1800

On terminal #4:

    $ java Server 1600 1700 1800

On terminal #5 and #6:

    $ java Client

Then, on terminal #5 or #6, try any query (e.g. "samsung", "iPhone", etc...).


