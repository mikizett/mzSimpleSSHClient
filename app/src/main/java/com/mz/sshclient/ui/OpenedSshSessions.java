package com.mz.sshclient.ui;

import com.mz.sshclient.model.SessionItemModel;
import com.mz.sshclient.ssh.SshTtyConnector;
import com.mz.sshclient.ssh.sftp.SFtpConnector;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public final class OpenedSshSessions {
    private OpenedSshSessions() {}

    private static final Queue<SshSessionHolder> openSshSessions = new ConcurrentLinkedQueue<>();

    public static boolean hasSameTabName(final String name) {
        return openSshSessions.stream().filter(item -> item.getSessionItemModel().getName().equals(name)).count() > 0;
    }

    public static void addSshSession(
            final SessionItemModel sessionItemModel,
            final SshTtyConnector sshTtyConnector,
            final SFtpConnector sFtpConnector
    ) {
        addSshSession(sessionItemModel, sshTtyConnector, sFtpConnector, Integer.MIN_VALUE);
    }

    public static void addSshSession(
            final SessionItemModel sessionItemModel,
            final SshTtyConnector sshTtyConnector,
            final SFtpConnector sFtpConnector,
            int index
    ) {
        openSshSessions.add(new SshSessionHolder(sessionItemModel, sshTtyConnector, sFtpConnector, index));
    }

    public static boolean removeSshSession(final SshSessionHolder sshSessionHolder) {
        // make sure the connections are closed
        sshSessionHolder.getSshTtyConnector().close();
        sshSessionHolder.getSFtpConnector().close();
        return openSshSessions.remove(sshSessionHolder);
    }

    public static void closeAllSshSessions() {
        final Iterator<SshSessionHolder> it = openSshSessions.iterator();
        while (it.hasNext()) {
            final SshSessionHolder holder = it.next();
            holder.getSshTtyConnector().close();
            holder.getSFtpConnector().close();
        }
    }

    public static Queue<SshSessionHolder> getOpenSshSessions() {
        return openSshSessions;
    }

    public static boolean hasOpenedSessions() {
        return !openSshSessions.isEmpty();
    }

    /**
     * Holds opened ssh sessions
     */
    @AllArgsConstructor
    @Getter
    public static final class SshSessionHolder {
        private final SessionItemModel sessionItemModel;
        private final SshTtyConnector sshTtyConnector;
        private final SFtpConnector sFtpConnector;
        private final int index;
    }
}
