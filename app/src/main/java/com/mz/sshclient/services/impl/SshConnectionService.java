package com.mz.sshclient.services.impl;

import com.mz.sshclient.services.events.ConnectSshEvent;
import com.mz.sshclient.services.events.listener.ISshConnectionListener;
import com.mz.sshclient.services.interfaces.ISshConnectionObservableService;

import java.util.ArrayList;
import java.util.List;

public class SshConnectionService implements ISshConnectionObservableService {

    private final List<ISshConnectionListener> connectSSHListeners = new ArrayList<>(0);

    @Override
    public void addConnectSshListener(ISshConnectionListener l) {
        if (!connectSSHListeners.contains(l)) {
            connectSSHListeners.add(l);
        }
    }

    @Override
    public void fireConnectSshEvent(ConnectSshEvent event) {
        if (event != null) {
            connectSSHListeners.forEach(l -> l.connectSsh(event));
        }
    }

    // FIXME: to be continued -> will be done as a refactoring task
}
