// Francesco Landolfi
// Matr. 444151
// fran.landofli@gmail.com

package simplesocial;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * An RMI stub. Represents a server on which a <b>SimpleSocial</b> client must register
 * to receive updates from other {@link simplesocial.server.User User}'s posts.
 *
 * @author Francesco Landolfi
 */
public interface Updater extends Remote {
    String OBJECT_NAME = "UPDATER";

    /**
     * Registers <code>username</code> to the server (if <code>oAuth</code> is valid).
     * A registered {@link simplesocial.server.User User} will be notified trough the
     * <code>follower</code> callback.
     *
     * @param follower the {@link simplesocial.server.User User}'s callback
     * @param username the {@link simplesocial.server.User User}'s name
     * @param oAuth the {@link simplesocial.server.User User}'s authorization token
     * @return <code>true</code> if registered, <code>false</code> otherwise
     * @throws RemoteException communication-related exception
     */
    boolean register(Follower follower, String username, int oAuth) throws RemoteException;

    /**
     * Makes <code>followerName</code> follow <code>username</code> (if <code>oAuth</code>
     * is valid).
     *
     * @param username the {@link simplesocial.server.User User} to be followed
     * @param followerName the follower's name
     * @param oAuth the follower's authorization token
     * @return an {@link ErrorCode}
     * @throws RemoteException communication-related exception
     */
    ErrorCode follow(String username, String followerName, int oAuth) throws RemoteException;
}
