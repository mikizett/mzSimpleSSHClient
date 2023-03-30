package com.mz.sshclient.ssh.sftp;

import com.mz.sshclient.model.SessionItemModel;
import com.mz.sshclient.ssh.SshClient;
import com.mz.sshclient.ssh.exceptions.SshOperationCanceledException;
import com.mz.sshclient.ssh.sftp.filesystem.sftp.SshFileSystem;
import net.schmizz.sshj.connection.channel.direct.Session;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

public class SFtpConnector {

    private static final Logger LOG = LogManager.getLogger(SFtpConnector.class);

    private final SessionItemModel sessionItemModel;
    private final SshClient sshClient;
    private final SshFileSystem sshFs;
    private volatile boolean closed = false;
    private volatile boolean opened = false;

    public SFtpConnector(final SessionItemModel sessionItemModel, final SshClient sshClient) {
        this.sessionItemModel = sessionItemModel;
        this.sshClient = sshClient;
        this.sshFs = new SshFileSystem(sshClient);
    }

    public SessionItemModel getSessionItemModel() {
        return sessionItemModel;
    }

    public int exec(String command, Function<Session.Command, Integer> callback, boolean pty) throws Exception {
        synchronized (sshClient) {
            if (closed) {
                throw new SshOperationCanceledException("ssh session closed by user");
            }
            try {
                try (Session session = sshClient.openSession()) {
                    session.setAutoExpand(true);
                    if (pty) {
                        session.allocatePTY("vt100", 80, 24, 0, 0, Collections.emptyMap());
                    }
                    try (final Session.Command cmd = session.exec(command)) {
                        return callback.apply(cmd);
                    }
                }
            } catch (Exception e) {
                LOG.error(e);
            }
            return 1;
        }
    }

    public int exec(String command, AtomicBoolean stopFlag) throws Exception {
        return exec(command, stopFlag, null, null);
    }

    public int exec(String command, AtomicBoolean stopFlag, StringBuilder stringBuilderOutput) throws Exception {
        return exec(command, stopFlag, stringBuilderOutput, null);
    }

    public int exec(String command, AtomicBoolean stopFlag, StringBuilder stringBuilderOutput, StringBuilder stringBuilderError) throws Exception {
        ByteArrayOutputStream byteArrayOutputStream = stringBuilderOutput == null ? null : new ByteArrayOutputStream();
        ByteArrayOutputStream byteArrayOutputStreamError = stringBuilderError == null ? null : new ByteArrayOutputStream();
        int ret = execBin(command, stopFlag, byteArrayOutputStream, byteArrayOutputStreamError);
        if (stringBuilderOutput != null) {
            stringBuilderOutput.append(byteArrayOutputStream.toString(StandardCharsets.UTF_8));
        }
        if (stringBuilderError != null) {
            stringBuilderError.append(byteArrayOutputStreamError.toString(StandardCharsets.UTF_8));
        }
        return ret;
    }

    public int execBin(String command, AtomicBoolean stopFlag, OutputStream outputStream, OutputStream outputStreamError) throws Exception {
        synchronized (sshClient) {
            if (closed) {
                throw new SshOperationCanceledException("ssh session closed");
            }
            LOG.debug("Current thread: " + Thread.currentThread().getName());
            LOG.debug("CMD: " + command);
            if (stopFlag.get()) {
                return -1;
            }
            try {
                if (!sshClient.isConnected()) {
                    sshClient.connect();
                }
                try (Session session = sshClient.openSession()) {
                    session.setAutoExpand(true);
                    try (final Session.Command cmd = session.exec(command)) {
                        LOG.debug("Command and Session started");

                        InputStream in = cmd.getInputStream();
                        InputStream err = cmd.getErrorStream();

                        byte[] b = new byte[8192];

                        do {
                            if (stopFlag.get()) {
                                LOG.debug("stopflag");
                                break;
                            }

                            if (in.available() > 0) {
                                int m = in.available();
                                while (m > 0) {
                                    int x = in.read(b, 0, m > b.length ? b.length : m);
                                    if (x == -1) {
                                        break;
                                    }
                                    m -= x;
                                    if (outputStream != null) {
                                        outputStream.write(b, 0, x);
                                    }

                                }
                            }

                            if (err.available() > 0) {
                                int m = err.available();
                                while (m > 0) {
                                    int x = err.read(b, 0, m > b.length ? b.length : m);
                                    if (x == -1) {
                                        break;
                                    }
                                    m -= x;
                                    if (outputStreamError != null) {
                                        outputStreamError.write(b, 0, x);
                                    }

                                }
                            }

                        } while (cmd.isOpen());

                        LOG.debug(cmd.isOpen() + " " + cmd.isEOF() + " " + cmd.getExitStatus());
                        LOG.debug("Command and Session closed");

                        cmd.close();
                        return cmd.getExitStatus();
                    }
                }
            } catch (Exception e) {
                LOG.error(e);
            }
            return 1;
        }
    }

    public void close() {
        if (sshClient.isConnected() && !isSessionClosed()) {
            try {
                closed = true;
                try {
                    sshFs.close();
                } catch (Exception e) {
                    LOG.error(e);
                }
                sshClient.close();
            } catch (IOException e) {
                LOG.error(e);
            }
        }
    }

    public SshFileSystem getSshFs() {
        return sshFs;
    }

    public boolean isSessionClosed() {
        return closed;
    }

    public SshClient getSshClient() {
        return sshClient;
    }

}
