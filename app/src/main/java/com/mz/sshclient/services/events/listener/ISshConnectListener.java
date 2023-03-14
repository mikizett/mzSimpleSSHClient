package com.mz.sshclient.services.events.listener;

import com.mz.sshclient.services.events.ConnectSSHEvent;

public interface ISshConnectListener {
    void connectSSH(ConnectSSHEvent event);
}
