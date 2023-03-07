package com.mz.sshclient.services.interfaces;

import com.mz.sshclient.services.events.ConnectSSHEvent;
import com.mz.sshclient.services.events.listener.IConnectSSHListener;

public interface ISSHConnectionService extends IService {
    void addConnectSSHListener(IConnectSSHListener l);

    void fireConnectSSHEvent(ConnectSSHEvent event);
}
