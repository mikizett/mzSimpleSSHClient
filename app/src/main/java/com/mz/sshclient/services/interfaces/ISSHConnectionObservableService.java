package com.mz.sshclient.services.interfaces;

import com.mz.sshclient.services.events.ConnectSshEvent;
import com.mz.sshclient.services.events.listener.ISshConnectionListener;

public interface ISSHConnectionObservableService extends IService {

    void addConnectSshListener(ISshConnectionListener l);

    void fireConnectSSHEvent(ConnectSshEvent event);

}