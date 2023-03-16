package com.mz.sshclient.ui;

import com.mz.sshclient.model.SessionItemModel;
import com.mz.sshclient.ssh.SshTtyConnector;
import com.mz.sshclient.ui.components.terminal.TabContentPanel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class OpenedSshSessions {
    private OpenedSshSessions() {}

    private static final List<SshSessionHolder> openSshSessions = new ArrayList<>(0);

    public static void addSshSession(final TabContentPanel tabContentPanel, final SessionItemModel sessionItemModel, final SshTtyConnector sshTtyConnector) {
        openSshSessions.add(new SshSessionHolder(sessionItemModel, tabContentPanel, sshTtyConnector));
    }

    public static boolean removeSshSession(final SshSessionHolder sshSessionHolder) {
        return openSshSessions.remove(sshSessionHolder);
    }

    public static void closeAllSshSessions() {
        final Iterator<SshSessionHolder> it = openSshSessions.iterator();
        while (it.hasNext()) {
            final SshSessionHolder holder = it.next();
            holder.getSshTtyConnector().close();
        };
    }

    public static List<SshSessionHolder> getOpenSshSessions() {
        return openSshSessions;
    }

    /**
     * Holds opened ssh sessions
     */
    @AllArgsConstructor
    @Getter
    public static final class SshSessionHolder {
        private final SessionItemModel sessionItemModel;
        private final TabContentPanel tabContentPanel;
        private final SshTtyConnector sshTtyConnector;
    }
}