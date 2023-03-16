package com.mz.sshclient.ssh;

import com.jediterm.terminal.Questioner;
import com.jediterm.terminal.TtyConnector;
import com.mz.sshclient.mzSimpleSshClientMain;
import com.mz.sshclient.ssh.exceptions.SshConnectionException;
import com.mz.sshclient.ssh.exceptions.SshDisconnectException;
import com.mz.sshclient.ssh.exceptions.SshOperationCanceledException;
import com.mz.sshclient.ui.utils.MessageDisplayUtil;
import net.schmizz.sshj.connection.ConnectionException;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.connection.channel.direct.SessionChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

public class SshTtyConnector implements TtyConnector {

    private static final Logger LOG = LogManager.getLogger(SshTtyConnector.class);

    private InputStreamReader inputStreamReader;
    private OutputStream outputStream;
    private SessionChannel shell;
    private Session channel;

    private boolean initiated = false;

    private IClosedSshConnectionCallback closedSshConnectionCallback;

    private final SshClient sshClient;

    public SshTtyConnector(final SshClient sshClient) {
        this.sshClient = sshClient;
    }

    public void addClosedSshConnectionCallback(IClosedSshConnectionCallback callback) {
        this.closedSshConnectionCallback = callback;
    }

    public boolean isRunning() {
        return shell != null && shell.isOpen();
    }

    @Override
    public boolean init(Questioner q) {
        try {
            channel = sshClient.openSession();
            channel.setAutoExpand(true);

            final int width = 80;
            final int height = 24;

            try {
                channel.allocatePTY("xterm-256color", width, height, 0, 0, Collections.emptyMap());
                channel.setEnvVar("LANG", "en_US.UTF-8");
                shell = (SessionChannel) this.channel.startShell();

                final InputStream inputStream = shell.getInputStream();
                outputStream = shell.getOutputStream();
                inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);

                initiated = true;
                return true;
            } catch (Exception e) {
                LOG.error(e);
                MessageDisplayUtil.showErrorMessage(mzSimpleSshClientMain.MAIN_FRAME, e.getMessage());
            }


        } catch (SshOperationCanceledException | SshConnectionException e) {
            LOG.error(e);
            MessageDisplayUtil.showErrorMessage(mzSimpleSshClientMain.MAIN_FRAME, e.getMessage());
            initiated = false;
        }
        return false;
    }

    @Override
    public void close() {
        try {
            sshClient.disconnect();

            if (closedSshConnectionCallback != null) {
                closedSshConnectionCallback.closedSshConnection(sshClient.getSessionItemModel());
            }

            LOG.info("Terminal wrapper disconnected");
        } catch (SshDisconnectException e) {
            LOG.error(e);
        }
    }

    @Override
    public String getName() {
        return "Remote";
    }

    @Override
    public int read(char[] buf, int offset, int length) throws IOException {
        return inputStreamReader.read(buf, offset, length);
    }

    @Override
    public void write(byte[] bytes) throws IOException {
        outputStream.write(bytes);
        outputStream.flush();
    }

    @Override
    public boolean isConnected() {
        return channel != null && channel.isOpen() && initiated;
    }

    @Override
    public void write(String string) throws IOException {
        write(string.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public int waitFor() throws InterruptedException {
        while (!initiated || isRunning()) {
            Thread.sleep(100);
        }

        try {
            shell.join();
        } catch (ConnectionException e) {
            LOG.error(e);
        }
        return shell.getExitStatus();
    }
}