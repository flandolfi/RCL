// Francesco Landolfi
// Matr. 444151
// fran.landofli@gmail.com

package simplesocial;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * An RMI callback stub. Represents a {@link simplesocial.server.User User}'s
 * follower (i.e. another online <b>SimpleSocial</b> client).
 *
 * @author Francesco Landolfi
 */
public interface Follower extends Remote {
    /**
     * Sends a {@link Post} to the follower.
     *
     * @param post the {@link Post} to be sent
     * @throws RemoteException communication-related exception
     */
    void sendUpdate(Post post) throws RemoteException;
}
