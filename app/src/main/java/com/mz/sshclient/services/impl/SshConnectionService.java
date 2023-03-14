package com.mz.sshclient.services.impl;

import com.mz.sshclient.services.events.ConnectSSHEvent;
import com.mz.sshclient.services.events.listener.ISshConnectListener;
import com.mz.sshclient.services.interfaces.ISSHConnectionObservableService;

import java.util.ArrayList;
import java.util.List;

public class SshConnectionService implements ISSHConnectionObservableService {

    private final List<ISshConnectListener> connectSSHListeners = new ArrayList<>(0);

    @Override
    public void addConnectSshListener(ISshConnectListener l) {
        if (!connectSSHListeners.contains(l)) {
            connectSSHListeners.add(l);
        }
    }

    @Override
    public void fireConnectSSHEvent(ConnectSSHEvent event) {
        if (event != null) {
            connectSSHListeners.forEach(l -> l.connectSSH(event));
        }
    }
}
