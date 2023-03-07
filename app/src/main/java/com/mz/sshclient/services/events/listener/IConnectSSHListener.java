package com.mz.sshclient.services.events.listener;

import com.mz.sshclient.services.events.ConnectSSHEvent;

public interface IConnectSSHListener {
    void connectSSH(ConnectSSHEvent event);
}
