// Francesco Landolfi
// Matr. 444151
// fran.landofli@gmail.com

import java.rmi.Remote;
import java.rmi.RemoteException;

// Conference STUB
public interface Conference extends Remote {
    public static final String OBJECT_NAME = "CONFERENCE";

    public void register(int day, int session, String speakerName) throws RemoteException;
    public String getProgramme() throws RemoteException;
}
