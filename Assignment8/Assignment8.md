MiniChatRoom
------------

Sviluppare un'applicazione di chat anonima dove gli utenti entrano senza registrarsi, ogni utente può inviare un messaggio e tutti gli utenti ricevono tutti i messaggi, senza sapere da chi vengono.

Il programma deve avere due componenti:

 + Server:

    - Lancia un servizio multicast per trasmettere i messaggi ricevuti.

    - Aspetta connessioni TCP da clienti

    - Quando una connessione viene stabilita, il server aspetta dei messaggi dal cliente su quella connessione, fin che la connessione viene chiusa dal cliente

    - Ogni volta che un messaggio arriva, il server lo invia a tutti gli clienti usando multicast. Se il messaggio ricevuto e più lungo di 512 byte, viene troncato.

    - Tutti i channel devono essere multiplexati usando un Selector in un solo thread (NIO, channel non-bloccanti).

   Tip: il channel multicast può essere inizialmente registrato con il selettore usando 0 come interestOps, poi aggiungere l'operazione SelectionKey.OP_WRITE quando si riceve un messaggio da un cliente.

 + Cliente:

    - Aderisce al gruppo multicast per ricevere messaggi dal server.

    - Si connette al server e gli invia dei messaggi scritti dall’utente alla riga di comando, fin che l’utente scrive “exit”.

    - Per ogni messaggio il cliente invia il testo del messaggio più il carattere ‘#’ per segnalare la fine del messaggio.

    - La lettura dal channel multicast e la scrittura nel channel TCP si fa nel thread main, usando Selector.

    - L’input dalla tastiera si gestisce in un altro thread (thread di lettura), usando una lista di messaggi per condividere l'input dell'utente con il thread main.

    - La possibile sovrapposizione del input del utente alla riga di comando con i messaggi arrivati dal server non deve essere trattata.

   Tip: il channel TCP può essere inizialmente registrato con il selettore usando 0 come interestOps, poi aggiungere l'operazione SelectionKey.OP_WRITE nel thread di lettura quando si inserisce un messaggio nella lista di messaggi. Quando la lista di messaggi ridiventa vuota, gli interestOps devono diventare di nuovo uguali a 0.   
