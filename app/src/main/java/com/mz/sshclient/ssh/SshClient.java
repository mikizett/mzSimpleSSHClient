package com.mz.sshclient.ssh;

import com.mz.sshclient.model.SessionItemModel;
import com.mz.sshclient.ssh.exceptions.SshConnectionException;
import com.mz.sshclient.ssh.exceptions.SshDisconnectException;
import com.mz.sshclient.ssh.exceptions.SshOperationCanceledException;
import com.mz.sshclient.ssh.exceptions.SshPrivateKeyMissingException;
import com.mz.sshclient.utils.Utils;
import net.schmizz.sshj.DefaultConfig;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.ConnectionException;
import net.schmizz.sshj.connection.channel.direct.DirectConnection;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.sftp.SFTPClient;
import net.schmizz.sshj.transport.TransportException;
import net.schmizz.sshj.userauth.UserAuthException;
import net.schmizz.sshj.userauth.keyprovider.KeyProvider;
import net.schmizz.sshj.userauth.method.AuthKeyboardInteractive;
import net.schmizz.sshj.userauth.method.AuthNone;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SshClient implements Closeable {

    private static final Logger LOG = LogManager.getLogger(SshClient.class);

    private static final int CONNECTION_TIMEOUT = 60000;

    private SSHClient sshj;

    private SshClient jumpHostSshClient;

    private volatile boolean closed = false;

    private final SessionItemModel  sessionItemModel;
    private final HostKeyVerifier hostKeyVerifier;
    private final IPrivateKeyPasswordFinderCallback privateKeyPasswordFinderCallback;
    private final IInteractiveResponseProvider interactiveResponseProvider;
    private final IPasswordRetryCallback passwordRetryCallback;

    public SshClient(
            final SessionItemModel sessionItemModel,
            final HostKeyVerifier hostKeyVerifier,
            final IPrivateKeyPasswordFinderCallback privateKeyPasswordFinderCallback,
            final IInteractiveResponseProvider interactiveResponseProvider,
            final IPasswordRetryCallback passwordRetryCallback
    ) {
        this.sessionItemModel = sessionItemModel;
        this.hostKeyVerifier = hostKeyVerifier;
        this.privateKeyPasswordFinderCallback = privateKeyPasswordFinderCallback;
        this.interactiveResponseProvider = interactiveResponseProvider;
        this.passwordRetryCallback = passwordRetryCallback;
    }

    private boolean hasJumpHost() {
        return StringUtils.isNotBlank(sessionItemModel.getJumpHost());
    }

    private void connectJumpHost() throws SshConnectionException, SshDisconnectException, SshOperationCanceledException {
        final SessionItemModel jumpHostItem = new SessionItemModel();
        jumpHostItem.setHost(sessionItemModel.getJumpHost());
        jumpHostItem.setPort(sessionItemModel.getPort());
        jumpHostItem.setUser(sessionItemModel.getUser());
        jumpHostItem.setPassword(sessionItemModel.getPassword());
        jumpHostItem.setPrivateKeyFile(sessionItemModel.getPrivateKeyFile());

        jumpHostSshClient = new SshClient(jumpHostItem, hostKeyVerifier, privateKeyPasswordFinderCallback, interactiveResponseProvider, passwordRetryCallback);
        jumpHostSshClient.connect();
    }

    private DirectConnection newDirectConnection(final String host, final String port) throws SshConnectionException {
        try {
            return sshj.newDirectConnection(host, Integer.parseInt(port));
        } catch (IOException e) {
            throw new SshConnectionException("Could not initialize new direct connection for the given jump host <" + sessionItemModel.getJumpHost() + ">", e);
        }
    }

    private void connectJumpHostViaTcpForwarding() throws SshConnectionException {
        try {
            if (jumpHostSshClient != null) {
                final DirectConnection directConnection = jumpHostSshClient.newDirectConnection(sessionItemModel.getHost(), sessionItemModel.getPort());
                this.sshj.connectVia(directConnection, sessionItemModel.getHost(), Integer.parseInt(sessionItemModel.getPort()));
            }
        } catch (SshConnectionException | IOException e) {
            String errorMsg;

            if (e.getCause() instanceof SshConnectionException) {
                errorMsg = e.getMessage();
            } else {
                errorMsg = "Could not connect via jump host";
            }

            throw new SshConnectionException(errorMsg, e);
        }
    }

    private void getAuthMethods(final List<String> allowedAuthMethods) {
        try {
            sshj.auth(sessionItemModel.getUser(), new AuthNone());
        } catch (UserAuthException | TransportException  e) {
            for (String method : sshj.getUserAuth().getAllowedMethods()) {
                allowedAuthMethods.add(method);
            }
        }
    }

    private void authPublicKey() throws SshConnectionException, SshDisconnectException, SshOperationCanceledException, SshPrivateKeyMissingException {
        if (StringUtils.isBlank(sessionItemModel.getPrivateKeyFile())) {
            throw new SshPrivateKeyMissingException("private key missing (not set from user)");
        }

        KeyProvider provider = null;

        final File keyFile = new File(sessionItemModel.getPrivateKeyFile());
        if (keyFile.exists()) {
            try {
                provider = sshj.loadKeys(sessionItemModel.getPrivateKeyFile(), privateKeyPasswordFinderCallback);

                LOG.debug("Key provider: " + provider);
                LOG.debug("Key type: " + provider.getType());
            } catch (IOException e) {
                throw new SshConnectionException("Could not load private/public key", e);
            }
        }

        if (closed) {
            disconnect();
            throw new SshOperationCanceledException("ssh connection closed by user: " + sessionItemModel);
        }

        if (provider == null) {
            throw new SshPrivateKeyMissingException("No suitable key providers (no private key selected).");
        }

        try {
            sshj.authPublickey(sessionItemModel.getUser(), provider);
        } catch (UserAuthException | TransportException e) {
            throw new SshConnectionException("Access Denied: Could not authenticate with private/public key", e);
        }
    }

    private void authPassword() throws SshOperationCanceledException, SshConnectionException {
        final String user = sessionItemModel.getUser();
        char[] decodedPassword = getDecodedPassword();

        while (!closed) {
            if (decodedPassword == null || decodedPassword.length < 1) {
                final char[] cachedEncodedPassword = passwordRetryCallback.getEncodedPassword(user);
                if (cachedEncodedPassword == null) {
                    throw new SshOperationCanceledException("Password not set");
                }
                decodedPassword = Utils.decodeCharArrayAsCharArray(cachedEncodedPassword);
            }
            try {
                sshj.authPassword(user, decodedPassword);
                break;
            } catch (UserAuthException | TransportException e) {
                throw new SshConnectionException("Wrong password", e);
            }
        }
    }

    private char[] getDecodedPassword() {
        char[] password = privateKeyPasswordFinderCallback.getEncodedCachedPassword();
        if (password == null && StringUtils.isNotBlank(sessionItemModel.getPassword())) {
            password = sessionItemModel.getPassword().toCharArray();
        }
        return Utils.decodeCharArrayAsCharArray(password);
    }

    public void connect() throws SshConnectionException, SshDisconnectException, SshOperationCanceledException {
        DefaultConfig defaultConfig = new DefaultConfig();
        //defaultConfig.setKeepAliveProvider(KeepAliveProvider.KEEP_ALIVE);

        sshj = new SSHClient(defaultConfig);
        sshj.setConnectTimeout(CONNECTION_TIMEOUT);
        sshj.setTimeout(CONNECTION_TIMEOUT);
        sshj.addHostKeyVerifier(hostKeyVerifier);

        try {
            if (!hasJumpHost()) {
                sshj.connect(sessionItemModel.getHost(), Integer.parseInt(sessionItemModel.getPort()));
            } else {
                connectJumpHost();
                connectJumpHostViaTcpForwarding();
            }
        } catch (IOException e) {
            throw new SshConnectionException("Could not connect to server: " + sessionItemModel.getHost(), e);
        }

        sshj.getConnection().getKeepAlive().setKeepAliveInterval(5);

        if (closed) {
            disconnect();
            throw new SshOperationCanceledException("ssh connection closed by user: " + sessionItemModel);
        }

        final List<String> allowedAuthMethods = new ArrayList<>(0);

        getAuthMethods(allowedAuthMethods);

        if (closed) {
            disconnect();
            throw new SshOperationCanceledException("ssh connection closed by user: " + sessionItemModel);
        }

        boolean authenticated = false;

        for (String authMethod : allowedAuthMethods) {
            if (closed) {
                disconnect();
                throw new SshOperationCanceledException("ssh connection closed by user: " + sessionItemModel);
            }

            LOG.debug("Trying auth method: " + authMethod);

            switch (authMethod) {
                case "publickey":
                    try {
                        authPublicKey();
                        authenticated = true;
                    } catch (SshOperationCanceledException e) {
                        disconnect();
                        throw e;
                    } catch (SshPrivateKeyMissingException e) {
                        LOG.warn(e);
                    }
                    break;

                case "keyboard-interactive":
                    try {
                        sshj.auth(sessionItemModel.getUser(), new AuthKeyboardInteractive(interactiveResponseProvider));
                        authenticated = true;
                    } catch (UserAuthException | TransportException e) {
                        LOG.warn("Could not authenticate keyboard-interactive", e);
                    }
                    break;

                case "password":
                    try {
                        authPassword();
                        authenticated = true;
                    } catch (SshOperationCanceledException e) {
                        disconnect();
                        throw e;
                    } catch (SshConnectionException e) {
                        LOG.warn(e);
                        throw e;
                    }
                    break;
            }
        }
        if (!authenticated) {
            if (this.sshj != null) {
                try {
                    this.sshj.close();
                } catch (IOException e) {
                    LOG.error(e);
                    // do nothing
                }
            }
            throw new SshConnectionException("Could not connect to server: " + sessionItemModel);
        }
    }

    public void disconnect() throws SshDisconnectException {
        if (closed) {
            LOG.info("ssh connection already closed: " + sessionItemModel);
            return;
        }

        closed = true;

        try {
            if (sshj != null) {
                sshj.disconnect();
            }
        } catch (IOException e) {
            throw new SshDisconnectException("Could not disconnect ssh connection: " + sessionItemModel, e);
        }

        try {
            if (jumpHostSshClient != null) {
                jumpHostSshClient.close();
            }
        } catch (IOException e) {
            throw new SshDisconnectException(e.getMessage(), e);
        }
    }

    public Session openSession() throws SshConnectionException, SshOperationCanceledException {
        try {
            if (closed) {
                disconnect();
            }
        } catch (SshDisconnectException e) {
            throw new SshOperationCanceledException("ssh closed by user");
        }

        Session session;
        try {
            session = sshj.startSession();
        } catch (ConnectionException | TransportException e) {
            throw new SshConnectionException("Could not create ssh session", e);
        }

        try {
            if (closed) {
                disconnect();
            }
        } catch (SshDisconnectException e) {
            throw new SshOperationCanceledException("ssh closed by user");
        }

        return session;
    }

    public boolean isConnected() {
        return sshj != null && sshj.isConnected();
    }

    public SessionItemModel getSessionItemModel() {
        return sessionItemModel;
    }

    public SFTPClient createSftpClient() throws IOException {
        return sshj.newSFTPClient();
    }

    @Override
    public void close() throws IOException {
        try {
            LOG.debug("Auto closing for: " + sessionItemModel);
            disconnect();
        } catch (SshDisconnectException e) {
            throw new IOException(e);
        }
    }

}
