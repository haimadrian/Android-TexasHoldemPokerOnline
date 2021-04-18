package org.hit.android.haim.calc.server.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.net.InetAddress;
import java.net.Socket;

/**
 * @author Haim Adrian
 * @since 17-Apr-21
 */
@AllArgsConstructor
@ToString
@Builder
public class ClientInfo {
    /**
     * The IP address of the remote end. (client)
     */
    @Getter
    private final InetAddress address;

    /**
     * The port number on the remote host. (client)
     */
    @Getter
    private final int port;

    /**
     * The local port number which we use in order to communicate with the remote client
     */
    @Getter
    private final int localPort;

    public static ClientInfo from(Socket socket) {
        return (socket == null) ? null : new ClientInfo(socket.getInetAddress(), socket.getPort(), socket.getLocalPort());
    }
}

