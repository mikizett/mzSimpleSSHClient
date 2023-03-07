package com.mz.sshclient.services.impl;

import com.mz.sshclient.services.events.ConnectSSHEvent;
import com.mz.sshclient.services.events.listener.IConnectSSHListener;
import com.mz.sshclient.services.interfaces.ISSHConnectionService;

import java.util.ArrayList;
import java.util.List;

public class SSHConnectionService implements ISSHConnectionService {

    private final List<IConnectSSHListener> connectSSHListeners = new ArrayList<>(0);

    @Override
    public void addConnectSSHListener(IConnectSSHListener l) {
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
